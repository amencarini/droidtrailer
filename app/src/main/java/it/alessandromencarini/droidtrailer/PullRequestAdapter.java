package it.alessandromencarini.droidtrailer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

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
        authorTextView.setText("@" + pullRequest.getUserLogin());

        TextView repositoryTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_repository);
        repositoryTextView.setText(pullRequest.getRepository().getFullName());

        TextView createdAtTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_createdAt);
        createdAtTextView.setText(pullRequest.getCreatedAt().toString());

        TextView commentCountTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_commentCountTextView);
        commentCountTextView.setText(pullRequest.getCommentCount().toString());

        TextView unreadCommentCountTextView = (TextView)rowView.findViewById(R.id.list_item_pull_request_unreadCommentCountTextView);
        if (pullRequest.getUnreadCommentCount() > 0) {
            unreadCommentCountTextView.setText(pullRequest.getUnreadCommentCount().toString());
            unreadCommentCountTextView.setVisibility(View.VISIBLE);
        } else {
            unreadCommentCountTextView.setVisibility(View.INVISIBLE);
        }

        ImageView userAvatarImageView = (ImageView)rowView.findViewById(R.id.list_item_pull_request_userAvatarImageView);
        userAvatarImageView.setImageResource(R.drawable.octocat);

        return rowView;
    }
}
