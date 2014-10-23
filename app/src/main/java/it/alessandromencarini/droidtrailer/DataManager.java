package it.alessandromencarini.droidtrailer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ale on 21/10/2014.
 */
public class DataManager {
    private static RepositoryDatabaseHelper sRepositoryHelper;
    private static PullRequestDatabaseHelper sPullRequestHelper;
    private static CommentDatabaseHelper sCommentHelper;
    private static DataManager sDataManager;
    private Context mAppContext;

    private DataManager(Context c) {
        mAppContext = c;
        sRepositoryHelper = new RepositoryDatabaseHelper(mAppContext);
        sPullRequestHelper = new PullRequestDatabaseHelper(mAppContext);
        sCommentHelper = new CommentDatabaseHelper(mAppContext);
    }

    public static DataManager get(Context c) {
        if (sDataManager == null)
            sDataManager = new DataManager(c.getApplicationContext());

        return sDataManager;
    }

    public ArrayList<Repository> getRepositories() {
        return sRepositoryHelper.getAllRepositories();
    }

    public ArrayList<Repository> getSelectedRepositories() {
        return sRepositoryHelper.getSelectedRepositories();
    }

//    public Repository getRepository(long id) {
//        // do something
//        return new Repository();
//    }

    public void save(Repository r) {
        sRepositoryHelper.insert(r);
    }

    public void clearUnselectedRepositories() {
        sRepositoryHelper.deletePullRequestsFromUnselectedRepositories();
    }

    public ArrayList<PullRequest> getPullRequests() {
        return sPullRequestHelper.getAllPullRequests();
    }

    public void save(PullRequest pr) {
        sPullRequestHelper.insert(pr);
    }

    public ArrayList<Comment> getComments() {
        return sCommentHelper.getAll();
    }

    public List<Comment> getNewComments(PullRequest pr) {
        return sCommentHelper.getNewComments(pr);
    }

    public void save(Comment c) {
        sCommentHelper.insert(c);
    }

    public void update(Comment c) {
        sCommentHelper.update(c);
    }

    public void update(PullRequest pr) {
        sPullRequestHelper.update(pr);
    }

    public void update(Repository r) {
        sRepositoryHelper.update(r);
    }

    public void delete(Comment c) {
        sCommentHelper.delete(c);
    }
}
