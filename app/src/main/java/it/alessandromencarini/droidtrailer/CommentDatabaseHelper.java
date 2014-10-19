package it.alessandromencarini.droidtrailer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessandromencarini on 23/09/2014.
 */
public class CommentDatabaseHelper extends DatabaseHelper {

    private CommentDao mCommentDao;

    public CommentDatabaseHelper(Context context) {
        super(context);
        mCommentDao = mDaoSession.getCommentDao();
    }

//    public ArrayList<Repository> getAllPullRequests() {
//        return (ArrayList<Repository>)mRepositoryDao.queryBuilder().list();
//    }

    public ArrayList<Comment> getAll() {
        return (ArrayList<Comment>)mCommentDao.queryBuilder().list();
    }

    public void upsert(Comment comment) {
        mCommentDao.insertOrReplace(comment);
    }

    public void delete(Comment comment) {
        mCommentDao.delete(comment);
    }

    public List<Comment> getNewComments(PullRequest pullRequest) {
        return mCommentDao.queryBuilder()
                .where(
                        CommentDao.Properties.CreatedAt.ge(pullRequest.getReadAt()),
                        CommentDao.Properties.PullRequestId.eq(pullRequest.getId())
                )
                .list();
    }
}
