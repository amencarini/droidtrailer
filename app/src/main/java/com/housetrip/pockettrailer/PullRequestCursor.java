package com.housetrip.pockettrailer;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.text.SimpleDateFormat;

/**
 * Created by alessandromencarini on 19/09/2014.
 */
public class PullRequestCursor extends CursorWrapper {
    public PullRequestCursor(Cursor cursor) {
        super(cursor);
    }

    public PullRequest getPullRequest() {
        if (isBeforeFirst() || isAfterLast())
            return null;

        long pullRequestId = getLong(getColumnIndex("_id"));
        String pullRequestTitle = getString(getColumnIndex("title"));
        String pullRequestAuthor = getString(getColumnIndex("author"));
        String pullRequestCreatedAt = getString(getColumnIndex("created_at"));
        String pullRequestUrl = getString(getColumnIndex("url"));

        PullRequest pullRequest = new PullRequest(pullRequestTitle, pullRequestAuthor, pullRequestCreatedAt, pullRequestUrl);
        pullRequest.setId(pullRequestId);

        return pullRequest;
    }
}