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

    public void insert(Comment... comments) {
        mCommentDao.insertInTx(comments);
    }
}
