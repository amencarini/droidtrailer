package com.housetrip.pockettrailer;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by alessandromencarini on 23/09/2014.
 */
public class RepositoryDatabaseHelper extends DatabaseHelper {

    private RepositoryDao mRepositoryDao;

    public RepositoryDatabaseHelper(Context context) {
        super(context);
        mRepositoryDao = mDaoSession.getRepositoryDao();
    }

//    public ArrayList<Repository> getAllPullRequests() {
//        return (ArrayList<Repository>)mRepositoryDao.queryBuilder().list();
//    }

    public long insert(Repository repository) {
        return mRepositoryDao.insert(repository);
    }

    public Repository findByFullName(String fullName) {
        try {
            return mRepositoryDao.queryBuilder()
                    .where(RepositoryDao.Properties.FullName.eq(fullName))
                    .list().get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
