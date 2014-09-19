package com.housetrip.pockettrailer;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by alessandromencarini on 18/09/2014.
 */
public class GithubFetcher {
    //    "https://api.github.com/repos/HouseTrip/HouseTrip-Web-App/pulls?access_token=6d126937d1b3e58bf1348aafd929279945f662f7"
    private static final String ENDPOINT = "https://api.github.com/repos/amencarini/dummy/pulls";
    private static final String API_KEY  = "6d126937d1b3e58bf1348aafd929279945f662f7";
    private static final String TAG      = "GithubFetcher";

    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<PullRequest> fetchItems() throws JSONException {
        ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();

        try {
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("access_token", API_KEY)
                    .build().toString();
            String result = new GithubFetcher().getUrl(url);

            JSONArray jsonObjects = new JSONArray(result);

            for (int i = 0; i < jsonObjects.length(); i++) {
                JSONObject pullRequest = jsonObjects.getJSONObject(i);
                pullRequests.add(new PullRequest(pullRequest));
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        }

        return pullRequests;
    }
}
