package it.alessandromencarini.droidtrailer;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PullRequestListActivity extends ListActivity {

    private ArrayList<PullRequest> mPullRequests;
    private PullRequestAdapter mPullRequestAdapter;

    private PullRequestDatabaseHelper mPullRequestHelper;
    private RepositoryDatabaseHelper mRepositoryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_request_list);

        mPullRequestHelper = new PullRequestDatabaseHelper(this);
        mRepositoryHelper = new RepositoryDatabaseHelper(this);

        mPullRequests = mPullRequestHelper.getAllPullRequests();

        mPullRequestAdapter = new PullRequestAdapter(this, mPullRequests);

        setListAdapter(mPullRequestAdapter);

        setTitle("All PRs");

    }

    private void refreshList(ArrayList<PullRequest> incomingPullRequests) {
        Repository repository = mRepositoryHelper.findByFullName("amencarini/dummy");
        if (repository == null) {
            repository = new Repository();
            repository.setFullName("amencarini/dummy");
            repository.setId(mRepositoryHelper.insert(repository));
        }

        // Get PRs that are already stored
        List<PullRequest> storedPullRequests = repository.getPullRequestList();

        // Mark for destruction, we'll preserve the ones that are still reported
        for (PullRequest storedPullRequest : storedPullRequests) {
            storedPullRequest.setMarkedForDestruction(true);
        }

        for (PullRequest incomingPullRequest : incomingPullRequests) {
            int position = storedPullRequests.indexOf(incomingPullRequest);
            incomingPullRequest.setRepositoryId(repository.getId());

            if (position == -1) {
                mPullRequestHelper.insert(incomingPullRequest);
                storedPullRequests.add(incomingPullRequest);
            } else {
                PullRequest storedPullRequest = storedPullRequests.get(position);
                storedPullRequest.setMarkedForDestruction(false);
                incomingPullRequest.setId(storedPullRequest.getId());
                mPullRequestHelper.update(incomingPullRequest);
                storedPullRequests.set(position, incomingPullRequest);
            }
        }

        for (PullRequest storedPullRequest : storedPullRequests) {
            if (storedPullRequest.getMarkedForDestruction()) {
                Toast.makeText(this, "Can't find PR " + storedPullRequest.getTitle(), Toast.LENGTH_LONG).show();
            }
        }

        mPullRequests.clear();

        for (PullRequest pullRequest : storedPullRequests) {
            mPullRequests.add(pullRequest);
        }

        mPullRequestAdapter.notifyDataSetChanged();
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
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.action_refresh:
                new FetchPullRequestsTask().execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        PullRequest pullRequest = mPullRequests.get(position);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.getUrl()));
        startActivity(browserIntent);
    }

    private class FetchPullRequestsTask extends AsyncTask<Void, Void, ArrayList<PullRequest>> {
        private static final String TAG = "FetchPullRequestsTask";

        @Override
        protected ArrayList<PullRequest> doInBackground(Void... params) {
            ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PullRequestListActivity.this);
                String apiKey = prefs.getString("github_key", "");
                pullRequests = new GithubFetcher(apiKey).fetchPullRequestsForRepository("amencarini/dummy");
            } catch (JSONException e) {
                Log.e(TAG, "JSON problems: ", e);
            }
            return pullRequests;
        }

        @Override
        protected void onPostExecute(ArrayList<PullRequest> pullRequests) {
            refreshList(pullRequests);
        }
    }
}
