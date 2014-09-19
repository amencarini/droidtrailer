package com.housetrip.pockettrailer;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;

public class PullRequestListActivity extends ListActivity {

    private ArrayList<PullRequest> mPullRequests;
//    private PullRequestAdapter mPullRequestAdapter;
    private PullRequestCursorAdapter mPullRequestAdapter;
    private PullRequestDatabaseHelper mHelper;
    private PullRequestCursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_request_list);

        ArrayList<PullRequest> mPullRequests = new ArrayList<PullRequest>();

        mHelper = new PullRequestDatabaseHelper(this);
        mCursor = mHelper.queryPullRequests();

//        mPullRequestAdapter = new PullRequestAdapter(this, mPullRequests);
        mPullRequestAdapter = new PullRequestCursorAdapter(this, mCursor);
        setListAdapter(mPullRequestAdapter);

        setTitle("All PRs");
//        new FetchPullRequestsTask().execute();
    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    private void setupAdapter() {
//        mPullRequestAdapter.clear();
//        mPullRequestAdapter.addAll(mPullRequests);
//        mPullRequestAdapter.notifyDataSetChanged();
//
//        for (PullRequest pullRequest : mPullRequests) {
//            mHelper.insertPullRequest(pullRequest);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pull_request_list, menu);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//        PullRequest pullRequest = mPullRequestAdapter.getItem(position);

        PullRequest pullRequest = null;
        PullRequestCursor cursor = mHelper.queryPullRequest(id);
        cursor.moveToFirst();
        if (!cursor.isAfterLast())
            pullRequest = cursor.getPullRequest();
        cursor.close();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.getUrl()));
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchPullRequestsTask extends AsyncTask<Void, Void, ArrayList<PullRequest>> {
        private static final String TAG = "FetchPullRequestsTask";

        @Override
        protected ArrayList<PullRequest> doInBackground(Void... params) {
            ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();
            try {
                pullRequests = new GithubFetcher().fetchItems();
            } catch (JSONException e) {
                Log.e(TAG, "JSON problems: ", e);
            }
            return pullRequests;
        }

        @Override
        protected void onPostExecute(ArrayList<PullRequest> pullRequests) {
            mPullRequests = pullRequests;

            setupAdapter();
        }
    }
}
