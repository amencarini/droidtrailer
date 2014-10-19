package it.alessandromencarini.droidtrailer;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PullRequestListActivity extends ListActivity {
    private final int REQUEST_CODE_REPOSITORY_ACTIVITY = 0;

    private ArrayList<PullRequest> mPullRequests;
    private PullRequestAdapter mPullRequestAdapter;

    private PullRequestDatabaseHelper mPullRequestHelper;
    private RepositoryDatabaseHelper mRepositoryHelper;
    private CommentDatabaseHelper mCommentHelper;

    protected ProgressDialog mDialog;

    // TODO: Transfer view handling to fragments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_request_list);

        mPullRequestHelper = new PullRequestDatabaseHelper(this);
        mRepositoryHelper = new RepositoryDatabaseHelper(this);

        mPullRequests = mPullRequestHelper.getAllPullRequests();

        mPullRequestAdapter = new PullRequestAdapter(this, mPullRequests);

        mCommentHelper = new CommentDatabaseHelper(this);

        setListAdapter(mPullRequestAdapter);

        setTitle("All PRs");

        mDialog = new ProgressDialog(this);

        refreshList();
    }

    private void refreshList() {
        // Get PRs that are already stored
        List<PullRequest> storedPullRequests = mPullRequestHelper.getAllPullRequests();

        mPullRequests.clear();

        for (PullRequest pullRequest : storedPullRequests) {
            mPullRequests.add(pullRequest);
        }

        mPullRequestAdapter.notifyDataSetChanged();

        mDialog.dismiss();
    }

    private void storePullRequests(ArrayList<PullRequest> incomingPullRequests) {
        List<PullRequest> storedPullRequests = mPullRequestHelper.getAllPullRequests();

        // Mark for destruction, we'll preserve the ones that are still reported
        for (PullRequest storedPullRequest : storedPullRequests) {
            storedPullRequest.setMarkedForDestruction(true);
        }

        for (PullRequest incomingPullRequest : incomingPullRequests) {
            int position = storedPullRequests.indexOf(incomingPullRequest);

            if (position == -1) {
                storedPullRequests.add(incomingPullRequest);
                incomingPullRequest.setReadAt(new Date());
                mPullRequestHelper.insert(incomingPullRequest);
            } else {
                Date storedReadAt = storedPullRequests.get(position).getReadAt();
                Date newReadAt = (storedReadAt != null) ? storedReadAt : new Date();
                incomingPullRequest.setReadAt(newReadAt);
                storedPullRequests.set(position, incomingPullRequest);
                mPullRequestHelper.update(incomingPullRequest);
            }
        }

        for (PullRequest storedPullRequest : storedPullRequests) {
            if (storedPullRequest.getMarkedForDestruction()) {
                new UpdatePullRequestTask(storedPullRequest).execute();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pull_request_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                new FetchPullRequestsTask().execute();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_repositories:
                startActivityForResult(new Intent(this, RepositoryActivity.class), REQUEST_CODE_REPOSITORY_ACTIVITY);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_REPOSITORY_ACTIVITY:
                new FetchPullRequestsTask().execute();
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        PullRequest pullRequest = mPullRequests.get(position);
        pullRequest.setUnreadCommentCount(0);
        pullRequest.setReadAt(new Date());
        pullRequest.update();
        refreshList();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.getUrl()));
        startActivity(browserIntent);
    }

    private void updatePullRequest(PullRequest pullRequest) {
        mPullRequestHelper.update(pullRequest);

        Resources r = getResources();
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.getUrl())), 0);

        Drawable blankDrawable = r.getDrawable(R.drawable.trailer_icon);
        Bitmap blankBitmap=((BitmapDrawable)blankDrawable).getBitmap();

        long timestamp;

        if (pullRequest.getCurrentState().equals("merged")) {
            timestamp = pullRequest.getMergedAt().getTime();
        } else if (pullRequest.getCurrentState().equals("closed")) {
            timestamp = pullRequest.getClosedAt().getTime();
        } else {
            timestamp = new Date().getTime();
        }

        Notification notification = new Notification.Builder(this)
                .setWhen(timestamp)
                .setShowWhen(true)
                .setTicker("PR " + pullRequest.getCurrentState() + "!")
                .setContentTitle("PR " + pullRequest.getCurrentState() + "!")
                .setContentText(pullRequest.getTitle())
                .setSmallIcon(R.drawable.trailer_icon)
                .setLargeIcon(blankBitmap)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

        int position = mPullRequests.indexOf(pullRequest);
        mPullRequests.set(position, pullRequest);
        mPullRequestAdapter.notifyDataSetChanged();
    }

    private void refreshComments() {
        ArrayList<PullRequest> pullRequests = mPullRequestHelper.getAllPullRequests();
        new FetchCommentsTask().execute(pullRequests);
    }

    private class FetchCommentsTask extends AsyncTask<ArrayList<PullRequest>, Void, ArrayList<Comment>> {
        private static final String TAG = "FetchCommentsTask";

        @Override
        protected ArrayList<Comment> doInBackground(ArrayList<PullRequest>... prs) {
            ArrayList<Comment> comments = new ArrayList<Comment>();

            ArrayList<PullRequest> newPullRequests = prs[0];

            try {
                comments = client().fetchCommentsForPullRequests(newPullRequests);
            } catch (IOException e) {
                Log.e(TAG, "Connection problems: ", e);
            }

            return comments;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> incomingComments) {
            ArrayList<Comment> storedComments = mCommentHelper.getAll();

            for (Comment storedComment : storedComments) {
                storedComment .setMarkedForDestruction(true);
            }

            for (Comment incomingComment : incomingComments) {
                int position = storedComments.indexOf(incomingComment);

                if (position == -1) {
                    storedComments.add(incomingComment);
                } else {
                    storedComments.set(position, incomingComment);
                }

                mCommentHelper.upsert(incomingComment);
            }

            for (Comment storedComment : storedComments) {
                if (storedComment.getMarkedForDestruction()) {
                    mCommentHelper.delete(storedComment);
                }
            }

            for (PullRequest pullRequest : mPullRequests) {
                List<Comment> newComments = mCommentHelper.getNewComments(pullRequest);
                pullRequest.setUnreadCommentCount(newComments.size());
                pullRequest.setCommentCount(pullRequest.getCommentList().size());
                pullRequest.update();
            }

            refreshList();
        }
    }

    private class UpdatePullRequestTask extends AsyncTask<Void, Void, PullRequest> {
        private static final String TAG = "UpdatePullRequestTask";
        private PullRequest mPullRequest;

        public UpdatePullRequestTask(PullRequest pullRequest) {
            super();
            mPullRequest = pullRequest;
        }

        @Override
        protected PullRequest doInBackground(Void... params) {
            String startingState = mPullRequest.getState();

            try {
                mPullRequest = client().updatePullRequest(mPullRequest);
            } catch (IOException e) {
                Log.e(TAG, "Connection problems: ", e);
            }

            if (mPullRequest.getState().equals(startingState)) {
                return null;
            } else {
                return mPullRequest;
            }
        }

        @Override
        protected void onPostExecute(PullRequest pullRequest) {
            if (pullRequest != null)
                updatePullRequest(pullRequest);
        }
    }

    private class FetchPullRequestsTask extends AsyncTask<Void, Void, ArrayList<PullRequest>> {
        private static final String TAG = "FetchPullRequestsTask";

        @Override
        protected void onPreExecute() {
            mDialog.setMessage("Loading...");
            mDialog.show();
        }

        @Override
        protected ArrayList<PullRequest> doInBackground(Void... params) {
            ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();

            ArrayList<Repository> selectedRepositories = mRepositoryHelper.getSelectedRepositories();

            for (Repository repository : selectedRepositories) {
                try {
                    pullRequests.addAll(client().fetchPullRequestsForRepository(repository));
                } catch (IOException e) {
                    Log.e(TAG, "Connection problems: ", e);
                }
            }

            return pullRequests;
        }

        @Override
        protected void onPostExecute(ArrayList<PullRequest> pullRequests) {
            storePullRequests(pullRequests);
            refreshComments();
        }
    }

    private String getApiKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PullRequestListActivity.this);
        return prefs.getString("github_key", "");
    }

    private GitHubFetcher client() {
        return new GitHubFetcher(getApiKey());
    }
}
