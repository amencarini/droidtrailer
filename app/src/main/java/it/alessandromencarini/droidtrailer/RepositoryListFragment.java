package it.alessandromencarini.droidtrailer;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RepositoryListFragment extends ListFragment {
    private List<Repository> mRepositories;
    private RepositoryAdapter mAdapter;
    private DataManager mDataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDataManager = DataManager.get(getActivity());

        getActivity().setTitle(R.string.title_activity_repository);
        mRepositories = mDataManager.getRepositories();

        sortRepositories();
        mAdapter = new RepositoryAdapter((ArrayList<Repository>) mRepositories);
        setListAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDataManager.clearUnselectedRepositories();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Repository r = ((RepositoryAdapter)getListAdapter()).getItem(position);
        r.setSelected(!r.getSelected());
        mDataManager.update(r);
        ((RepositoryAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_repository_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new FetchRepositoriesTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void storeNewRepositories(ArrayList<Repository> incomingRepositories) {
        List<Repository> storedRepositories = mDataManager.getRepositories();

        for (Repository incomingRepository : incomingRepositories) {
            int position = storedRepositories.indexOf(incomingRepository);

            if (position == -1) {
                mDataManager.save(incomingRepository);
                storedRepositories.add(incomingRepository);
            }
        }

        mRepositories.clear();
        mRepositories.addAll(storedRepositories);
        sortRepositories();
        mAdapter.notifyDataSetChanged();
    }

    private void sortRepositories() {
        Collections.sort(mRepositories, new SortRepositoriesByFullName());
    }

    private class RepositoryAdapter extends ArrayAdapter<Repository> {
        public RepositoryAdapter(ArrayList<Repository> repositories) {
            super(getActivity(), 0, repositories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Repository r = getItem(position);

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_repository, null);
            }

            TextView fullNameTextView = (TextView) convertView.findViewById(R.id.list_item_repository_fullNameTextView);
            fullNameTextView.setText(r.getFullName());

            CheckBox selectedCheckBox = (CheckBox) convertView.findViewById(R.id.list_item_repository_selectedCheckBox);
            selectedCheckBox.setChecked(r.getSelected());

//            selectedCheckBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    convertView
//                }
//            });

//            selectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    r.setSelected(b);
//                    mDataManager.update(r);
//                }
//            });

            return convertView;
        }
    }

    private class SortRepositoriesByFullName implements Comparator<Repository> {
        @Override
        public int compare(Repository r1, Repository r2) {
            return r1.getFullName().compareTo(r2.getFullName());
        }
    }

    private class FetchRepositoriesTask extends AsyncTask<Void, Void, ArrayList<Repository>> {
        private static final String TAG = "FetchRepositoriesTask";

        private ProgressDialog mDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            mDialog.setMessage("Loading...");
            mDialog.show();
        }

        @Override
        protected ArrayList<Repository> doInBackground(Void... params) {
            ArrayList<Repository> repositories = new ArrayList<Repository>();
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String apiKey = prefs.getString("github_key", "");
                repositories = new GitHubFetcher(apiKey, "").fetchRepositories();
            } catch (JSONException e) {
                Log.e(TAG, "JSON problems: ", e);
            } catch (IOException e) {
                Toast.makeText(getActivity(), "There was a problem with your last action.", Toast.LENGTH_LONG).show();
            }
            return repositories;
        }

        @Override
        protected void onPostExecute(ArrayList<Repository> repositories) {
            storeNewRepositories(repositories);
            mDialog.dismiss();
        }
    }
}
