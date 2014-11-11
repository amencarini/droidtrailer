package it.alessandromencarini.droidtrailer;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PullRequestListActivity extends SingleFragmentActivity {
    private SharedPreferences mPrefs;

    @Override
    protected Fragment createFragment() {
        return new PullRequestListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getKey().length() <= 0) {
            Intent i = new Intent(this, SettingsActivity.class);
            i.putExtra(SettingsActivity.FROM_MAIN_ACTIVITY, true);
            startActivity(i);
            overridePendingTransition(0,0);
        }

        super.onCreate(savedInstanceState);
    }

    private String getKey() {
        return mPrefs.getString("github_key", "");
    }
}
