package it.alessandromencarini.droidtrailer;

import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RepositoryActivity extends ListActivity {

    private RepositoryAdapter mRepositoryAdapter;
    private ArrayList<Repository> mRepositories;
    private RepositoryDatabaseHelper mRepositoryHelper;

    // TODO: Transfer view handling to fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository);

        mRepositoryHelper = new RepositoryDatabaseHelper(this);
        mRepositories = mRepositoryHelper.getAllRepositories();
        sortRepositories();

        mRepositoryAdapter = new RepositoryAdapter(this, mRepositories);
        setListAdapter(mRepositoryAdapter);
    }

    private void sortRepositories() {
        Collections.sort(mRepositories, new SortRepositoriesByFullName());
    }

    public void updateRepository(Repository repository) {
        mRepositoryHelper.update(repository);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.repository, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new FetchRepositoriesTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshList(ArrayList<Repository> incomingRepositories) {
        ArrayList<Repository> storedRepositories = mRepositoryHelper.getAllRepositories();

        for (Repository incomingRepository : incomingRepositories) {
            int position = storedRepositories.indexOf(incomingRepository);

            if (position == -1) {
                mRepositoryHelper.insert(incomingRepository);
                storedRepositories.add(incomingRepository);
            }
        }

        mRepositories.clear();
        mRepositories.addAll(storedRepositories);
        sortRepositories();
        mRepositoryAdapter.notifyDataSetChanged();
    }

    private class SortRepositoriesByFullName implements Comparator<Repository> {
        @Override
        public int compare(Repository repository, Repository repository2) {
            return repository.getFullName().compareTo(repository2.getFullName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRepositoryHelper.deletePullRequestsFromUnselectedRepositories();
    }

    private class FetchRepositoriesTask extends AsyncTask<Void, Void, ArrayList<Repository>> {
        private static final String TAG = "FetchRepositoriesTask";

        @Override
        protected ArrayList<Repository> doInBackground(Void... params) {
            ArrayList<Repository> repositories = new ArrayList<Repository>();
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RepositoryActivity.this);
                String apiKey = prefs.getString("github_key", "");
                repositories = new GithubFetcher(apiKey).fetchRepositories();
            } catch (JSONException e) {
                Log.e(TAG, "JSON problems: ", e);
            }
            return repositories;
        }

        @Override
        protected void onPostExecute(ArrayList<Repository> repositories) {
            refreshList(repositories);
        }
    }

}
