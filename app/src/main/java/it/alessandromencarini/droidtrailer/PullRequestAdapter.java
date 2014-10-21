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
    private AvatarDownloader<ImageView> mAvatarDownloader;

    public PullRequestAdapter(Context context, ArrayList<PullRequest> pullRequests, AvatarDownloader<ImageView> avatarDownloader) {
        super(context, R.layout.list_item_pull_request, pullRequests);
        mContext = context;
        mPullRequests = pullRequests;
        mAvatarDownloader = avatarDownloader;
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

        if (pullRequest.getUnreadCommentCount() > 0) {
            commentCountTextView.setBackgroundColor(Color.RED);
            commentCountTextView.setTextColor(Color.WHITE);
        } else {
            commentCountTextView.setBackgroundColor(Color.argb(255, 200, 200, 200));
            commentCountTextView.setTextColor(Color.BLACK);
        }

        ImageView userAvatarImageView = (ImageView)rowView.findViewById(R.id.list_item_pull_request_userAvatarImageView);
        userAvatarImageView.setImageResource(R.drawable.octocat);
        mAvatarDownloader.queueAvatar(userAvatarImageView, pullRequest.getUserAvatarUrl());

        return rowView;
    }
}
