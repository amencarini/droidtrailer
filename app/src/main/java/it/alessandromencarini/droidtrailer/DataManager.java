package it.alessandromencarini.droidtrailer;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by ale on 21/10/2014.
 */
public class DataManager {
    private ArrayList<Repository> mRepositories;
    private static RepositoryDatabaseHelper sRepositoryHelper;

    private static DataManager sDataManager;
    private Context mAppContext;

    private DataManager(Context appContext) {
        mAppContext = appContext;
        mRepositories = new ArrayList<Repository>();
        sRepositoryHelper = new RepositoryDatabaseHelper(mAppContext);
    }

    public static DataManager get(Context c) {
        if (sDataManager == null)
            sDataManager = new DataManager(c.getApplicationContext());

        return sDataManager;
    }

    public ArrayList<Repository> getRepositories() {
        return sRepositoryHelper.getAllRepositories();
    }

    public Repository getRepository(long id) {
        // do something
        return new Repository();
    }

    public static void save(Repository r) {
        sRepositoryHelper.insert(r);
    }

    public static void clearUnselectedRepositories() {
        sRepositoryHelper.deletePullRequestsFromUnselectedRepositories();
    }
}
