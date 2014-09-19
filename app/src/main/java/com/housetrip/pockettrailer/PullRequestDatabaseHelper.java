package com.housetrip.pockettrailer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alessandromencarini on 19/09/2014.
 */
public class PullRequestDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pull_requests.sqlite";
    private static final int VERSION = 1;

    public PullRequestDatabaseHelper(Context c) {
        super(c, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE pull_requests (" +
                "_id integer primary key autoincrement" +
                ", title string" +
                ", author string" +
                ", url string" +
                ", created_at string" +
                ", closed_at string" +
                ", merged_at string" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public long insertPullRequest(PullRequest pullRequest) {
        ContentValues cv = new ContentValues();
        cv.put("title", pullRequest.getTitle());
        cv.put("author", pullRequest.getAuthor());
        cv.put("url", pullRequest.getUrl());
        cv.put("created_at", PullRequest.GITHUB_DATE_FORMAT.format(pullRequest.getCreatedAt()));
        return getWritableDatabase().insert("pull_requests", null, cv);
    }

    public PullRequestCursor queryPullRequests() {
        Cursor wrapped = getReadableDatabase().query("pull_requests", null, null, null, null, null, "created_at DESC");
        return new PullRequestCursor(wrapped);
    }

    public PullRequestCursor queryPullRequest(long id) {
        Cursor wrapped = getReadableDatabase().query("pull_requests",
                null,
                "_id = ?",
                new String[]{ String.valueOf(id) },
                null,
                null,
                null,
                "1");
        return new PullRequestCursor(wrapped);
    }
}
