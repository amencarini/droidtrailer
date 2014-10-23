package it.alessandromencarini.droidtrailer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by alessandromencarini on 24/09/2014.
 */
public class DatabaseHelper {
    protected DaoSession mDaoSession;

    public DatabaseHelper(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "pull-requests-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }
}
