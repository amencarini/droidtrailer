package it.alessandromencarini.droidtrailer;

import android.app.Fragment;

public class PullRequestListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PullRequestListFragment();
    }
}
