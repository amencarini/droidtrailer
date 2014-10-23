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

    public ArrayList<Comment> getAll() {
        return (ArrayList<Comment>)mCommentDao.queryBuilder().list();
    }

    public List<Comment> getNewComments(PullRequest pr) {
        return mCommentDao.queryBuilder()
                .where(
                        CommentDao.Properties.PullRequestId.eq(pr.getId()),
                        CommentDao.Properties.CreatedAt.ge(pr.getReadAt())
                )
                .list();
    }

    public void insert(Comment c) {
        mCommentDao.insert(c);
    }

    public void update(Comment c) {
        mCommentDao.update(c);
    }

    public void delete(Comment c) {
        mCommentDao.delete(c);
    }
}
