package it.alessandromencarini.droidtrailer;

import android.app.Fragment;

public class RepositoryListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new RepositoryListFragment();
    }
}
