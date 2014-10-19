package it.alessandromencarini.droidtrailer;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by alessandromencarini on 23/09/2014.
 */
public class PullRequestDatabaseHelper extends DatabaseHelper {

    private PullRequestDao mPullRequestDao;

    public PullRequestDatabaseHelper(Context context) {
        super(context);
        mPullRequestDao = mDaoSession.getPullRequestDao();
    }

    public ArrayList<PullRequest> getAllPullRequests() {
        return (ArrayList<PullRequest>)mPullRequestDao.queryBuilder().list();
    }

    public long insert(PullRequest pullRequest) {
        return mPullRequestDao.insert(pullRequest);
    }

    public void update(PullRequest pullRequest) {
        mPullRequestDao.update(pullRequest);
    }
}
