package com.housetrip.pockettrailer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alessandromencarini on 18/09/2014.
 */
public class PullRequest {
    private long mId;
    private String mTitle;
    private String mAuthor;
    private String mUrl;
    private Date mCreatedAt;
    private Date mClosedAt;
    private Date mMergedAt;

    public final static SimpleDateFormat GITHUB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public PullRequest(String title, String author, String createdAt, String url) {
        mId = -1;
        mTitle = title;
        mAuthor = author;
        mCreatedAt = parseDate(createdAt);
        mUrl = url;
    }

    public PullRequest(JSONObject json) throws JSONException {
        mTitle = json.getString("title");
        mAuthor = json.getJSONObject("user").getString("login");
        mUrl = json.getString("html_url");
        mCreatedAt = parseDate(json.getString("created_at"));
        mClosedAt = parseDate(json.getString("closed_at"));
        mMergedAt = parseDate(json.getString("merged_at"));
    }

    private Date parseDate(String string) {
        if (string == "null")
            return null;

        try {
            return GITHUB_DATE_FORMAT.parse(string);
        } catch (ParseException e) {
            Log.e("PullRequest", "Could not parse date: ", e);
            return null;
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getUrl() {
        return mUrl;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getClosedAt() {
        return mClosedAt;
    }

    public Date getMergedAt() {
        return mMergedAt;
    }
}
