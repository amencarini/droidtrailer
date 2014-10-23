package it.alessandromencarini.droidtrailer;

import android.app.ListFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ale on 23/10/2014.
 */
public class PullRequestListFragment extends ListFragment {
    private final int REQUEST_CODE_REPOSITORY_ACTIVITY = 0;

    private ProgressDialog mDialog;
    private DataManager mDataManager;
    private GitHubFetcher mClient;
    private PullRequestAdapter mAdapter;
    private List<PullRequest> mPullRequests;
    private AvatarDownloader<ImageView> mAvatarThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        getActivity().setTitle(R.string.title_activity_pull_request_list);
        mDialog = new ProgressDialog(getActivity());
        mDataManager = DataManager.get(getActivity());
        mClient = new GitHubFetcher(getApiKey());

        mPullRequests = mDataManager.getPullRequests();
        mAdapter = new PullRequestAdapter((ArrayList<PullRequest>) mPullRequests);
        setListAdapter(mAdapter);

        mAvatarThread = new AvatarDownloader<ImageView>(mClient, new Handler());
        mAvatarThread.setListener(new AvatarDownloader.Listener<ImageView>() {
            @Override
            public void onAvatarDownloaded(ImageView imageView, Bitmap avatar) {
                imageView.setImageBitmap(avatar);
            }
        });
        mAvatarThread.start();
        mAvatarThread.getLooper();

        getActivity().setTitle(R.string.title_activity_pull_request_list);
        refreshList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAvatarThread.quit();
        mAvatarThread.clearQueue();
    }

    private void refreshList() {
        // Get PRs that are already stored
        List<PullRequest> storedPullRequests = mDataManager.getPullRequests();

        mPullRequests.clear();

        for (PullRequest pullRequest : storedPullRequests) {
            mPullRequests.add(pullRequest);
        }

        mAdapter.notifyDataSetChanged();

        mDialog.dismiss();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_pull_request_list, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        PullRequest pr = mPullRequests.get(position);
        pr.setReadAt(new Date());
        mDataManager.update(pr);
        mAdapter.notifyDataSetChanged();
//        refreshList();

        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(pr.getUrl()));
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new FetchPullRequestsTask().execute();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_repositories:
                startActivityForResult(new Intent(getActivity(), RepositoryListActivity.class), REQUEST_CODE_REPOSITORY_ACTIVITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_REPOSITORY_ACTIVITY:
                new FetchPullRequestsTask().execute();
        }
    }

    private void storePullRequests(ArrayList<PullRequest> incomingPullRequests) {
        List<PullRequest> storedPullRequests = mDataManager.getPullRequests();

        // Mark for destruction, we'll preserve the ones that are still reported
        for (PullRequest storedPullRequest : storedPullRequests) {
            storedPullRequest.setMarkedForDestruction(true);
        }

        for (PullRequest incomingPullRequest : incomingPullRequests) {
            int position = storedPullRequests.indexOf(incomingPullRequest);

            if (position == -1) {
                storedPullRequests.add(incomingPullRequest);
                incomingPullRequest.setReadAt(new Date());
                mDataManager.save(incomingPullRequest);
            } else {
                Date storedReadAt = storedPullRequests.get(position).getReadAt();
                Date newReadAt = (storedReadAt != null) ? storedReadAt : new Date();
                incomingPullRequest.setReadAt(newReadAt);
                storedPullRequests.set(position, incomingPullRequest);
                mDataManager.update(incomingPullRequest);
            }
        }

        for (PullRequest storedPullRequest : storedPullRequests) {
            if (storedPullRequest.getMarkedForDestruction()) {
                new UpdatePullRequestTask(storedPullRequest).execute();
            }
        }
    }

    private void updatePullRequest(PullRequest pullRequest) {
        mDataManager.update(pullRequest);

        Resources r = getResources();
//        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.getUrl())), 0);

        Drawable blankDrawable = r.getDrawable(R.drawable.trailer_icon);
        Bitmap blankBitmap = ((BitmapDrawable) blankDrawable).getBitmap();

        long timestamp;

        if (pullRequest.getCurrentState().equals("merged")) {
            timestamp = pullRequest.getMergedAt().getTime();
        } else if (pullRequest.getCurrentState().equals("closed")) {
            timestamp = pullRequest.getClosedAt().getTime();
        } else {
            timestamp = new Date().getTime();
        }

        Notification notification = new Notification.Builder(getActivity())
                .setWhen(timestamp)
                .setShowWhen(true)
                .setTicker("PR " + pullRequest.getCurrentState() + "!")
                .setContentTitle("PR " + pullRequest.getCurrentState() + "!")
                .setContentText(pullRequest.getTitle())
                .setSmallIcon(R.drawable.trailer_icon)
                .setLargeIcon(blankBitmap)
//                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(pullRequest.getRepository().getFullName(), pullRequest.getNumber(), notification);

        int position = mPullRequests.indexOf(pullRequest);
        mPullRequests.set(position, pullRequest);
        mAdapter.notifyDataSetChanged();
    }

    private void refreshComments() {
        new FetchCommentsTask().execute();
    }

    private void notifyComments(Comment comment) {
//        Resources r = getResources();

        // TODO: Set comment url
        // PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.getUrl())), 0);

        Notification notification = new Notification.Builder(getActivity())
                .setWhen(comment.getCreatedAt().getTime())
                .setShowWhen(true)
                .setTicker("New comment")
                .setContentTitle("New comment")
                .setContentText(comment.getBody())
                .setSmallIcon(R.drawable.trailer_icon)
                        // .setLargeIcon(bitmap)
                        // .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(comment.getPullRequest().getRepository().getFullName(), comment.getPullRequest().getNumber(), notification);
    }

    private String getApiKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString("github_key", "");
    }

    private class PullRequestAdapter extends ArrayAdapter<PullRequest> {
        public PullRequestAdapter(ArrayList<PullRequest> pullRequests) {
            super(getActivity(), 0, pullRequests);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_pull_request, null);
            }

            PullRequest pr = getItem(position);

            TextView titleTextView = (TextView) convertView.findViewById(R.id.list_item_pull_request_title);
            titleTextView.setText(pr.getTitle());

            TextView authorTextView = (TextView) convertView.findViewById(R.id.list_item_pull_request_author);
            authorTextView.setText("@" + pr.getUserLogin());

            TextView repositoryTextView = (TextView) convertView.findViewById(R.id.list_item_pull_request_repository);
            repositoryTextView.setText(pr.getRepository().getFullName());

            TextView createdAtTextView = (TextView) convertView.findViewById(R.id.list_item_pull_request_createdAt);
            createdAtTextView.setText(pr.getCreatedAt().toString());

            TextView commentCountTextView = (TextView) convertView.findViewById(R.id.list_item_pull_request_commentCountTextView);
            commentCountTextView.setText(Integer.toString(pr.getCommentList().size()));

            if (mDataManager.getNewComments(pr).size() > 0) {
                commentCountTextView.setBackgroundColor(Color.RED);
                commentCountTextView.setTextColor(Color.WHITE);
            } else {
                commentCountTextView.setBackgroundColor(Color.argb(255, 200, 200, 200));
                commentCountTextView.setTextColor(Color.BLACK);
            }

            ImageView userAvatarImageView = (ImageView) convertView.findViewById(R.id.list_item_pull_request_userAvatarImageView);
            userAvatarImageView.setImageResource(R.drawable.octocat);
            mAvatarThread.queueAvatar(userAvatarImageView, pr.getUserAvatarUrl());

            return convertView;
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

            List<Repository> selectedRepositories = mDataManager.getSelectedRepositories();

            for (Repository repository : selectedRepositories) {
                try {
                    pullRequests.addAll(mClient.fetchPullRequestsForRepository(repository));
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
                mPullRequest = mClient.updatePullRequest(mPullRequest);
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

    private class FetchCommentsTask extends AsyncTask<Void, Void, ArrayList<Comment>> {
        private static final String TAG = "FetchCommentsTask";

        @Override
        protected ArrayList<Comment> doInBackground(Void... voids) {
            ArrayList<Comment> comments = new ArrayList<Comment>();

            try {
                comments = mClient.fetchCommentsForPullRequests(mDataManager.getPullRequests());
            } catch (IOException e) {
                Log.e(TAG, "Connection problems: ", e);
            }

            return comments;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> incomingComments) {
            ArrayList<Comment> storedComments = mDataManager.getComments();

            Comment newComment = null;

            for (Comment storedComment : storedComments) {
                storedComment.setMarkedForDestruction(true);
            }

            for (Comment incomingComment : incomingComments) {
                int position = storedComments.indexOf(incomingComment);

                if (position == -1) {
                    storedComments.add(incomingComment);
                    newComment = incomingComment;
                    mDataManager.save(incomingComment);
                } else {
                    storedComments.set(position, incomingComment);
                    mDataManager.update(incomingComment);
                }
            }

            for (Comment storedComment : storedComments) {
                if (storedComment.getMarkedForDestruction()) {
                    mDataManager.delete(storedComment);
                }
            }

            for (PullRequest pr : mPullRequests) {
                pr.resetCommentList();
                pr.setCommentCount(pr.getCommentList().size());
                mDataManager.update(pr);
            }

            refreshList();

            if (newComment != null)
                notifyComments(newComment);
        }
    }
}
