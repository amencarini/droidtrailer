package it.alessandromencarini.droidtrailer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

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

    public ArrayList<Repository> getAllRepositories() {
        return (ArrayList<Repository>)mRepositoryDao.queryBuilder().list();
    }

    public long insert(Repository repository) {
        return mRepositoryDao.insert(repository);
    }

//    public Repository findByFullName(String fullName) {
//        try {
//            return mRepositoryDao.queryBuilder()
//                    .where(RepositoryDao.Properties.FullName.eq(fullName))
//                    .list().get(0);
//        } catch (IndexOutOfBoundsException e) {
//            return null;
//        }
//    }

    public void update(Repository repository) {
        mRepositoryDao.update(repository);
    }

    public ArrayList<Repository> getSelectedRepositories() {
        return (ArrayList<Repository>)mRepositoryDao.queryBuilder()
                .where(RepositoryDao.Properties.Selected.eq(true))
                .list();
    }

    public void deletePullRequestsFromUnselectedRepositories() {
        List<Repository> unselectedRepositories = mRepositoryDao.queryBuilder()
                .whereOr(
                        RepositoryDao.Properties.Selected.eq(false),
                        RepositoryDao.Properties.Selected.isNull()
                ).list();

        for (Repository repository : unselectedRepositories) {
            for (PullRequest pullRequest : repository.getPullRequestList()) {
                mDaoSession.delete(pullRequest);
            }
        }
    }
}
