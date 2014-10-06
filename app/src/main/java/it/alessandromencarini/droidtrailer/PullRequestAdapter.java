package it.alessandromencarini.droidtrailer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alessandromencarini on 18/09/2014.
 */
public class PullRequestAdapter extends ArrayAdapter<PullRequest> {
    private final Context mContext;
    private ArrayList<PullRequest> mPullRequests;

    public PullRequestAdapter(Context context, ArrayList<PullRequest> pullRequests) {
        super(context, R.layout.list_item_pull_request, pullRequests);
        mContext = context;
        mPullRequests = pullRequests;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_pull_request, parent, false);

        PullRequest pullRequest = mPullRequests.get(position);

        TextView titleTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_title);
        titleTextView.setText(pullRequest.getTitle());

        TextView authorTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_author);
        authorTextView.setText(pullRequest.getUserLogin());

        TextView stateTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_state);
        stateTextView.setText(pullRequest.getState());

        TextView commentCountTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_commentCountTextView);
        commentCountTextView .setText(pullRequest.getCommentCount());
        if (pullRequest.getUnreadCommentCount() > 0) {
            commentCountTextView.setTextColor(Color.RED);
        }

        return rowView;
    }
}
