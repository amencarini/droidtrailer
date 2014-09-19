package com.housetrip.pockettrailer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by alessandromencarini on 19/09/2014.
 */
public class PullRequestCursorAdapter extends CursorAdapter {

    private PullRequestCursor mCursor;

    public PullRequestCursorAdapter(Context context, PullRequestCursor cursor) {
        super(context, cursor, 0);
        mCursor = cursor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.list_item_pull_request, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        PullRequest pullRequest = mCursor.getPullRequest();

        TextView titleTextView = (TextView)view.findViewById(R.id.list_item_pull_request_title);
        titleTextView.setText(pullRequest.getTitle());

        TextView authorTextView = (TextView)view.findViewById(R.id.list_item_pull_request_author);
        authorTextView.setText(pullRequest.getAuthor());
    }
}
