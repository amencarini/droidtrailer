package it.alessandromencarini.droidtrailer;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private String mApiKey;

    private static final String TAG      = "GithubFetcher";

    //    "https://api.github.com/repos/HouseTrip/HouseTrip-Web-App/pulls?access_token=6d126937d1b3e58bf1348aafd929279945f662f7"
    private static final String ENDPOINT = "https://api.github.com";
    private static final String PART_REPOSITORY = "/repos";
    private static final String PART_SUBSCRIPTIONS = "/user/subscriptions";
    private static final String PART_PULL_REQUESTS = "/pulls";

    public GithubFetcher(String apiKey) {
        mApiKey = apiKey;
    }

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

    public ArrayList<PullRequest> fetchPullRequestsForRepository(Repository repository) throws JSONException {
        ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();

        // TODO: Block requests if apikey is not available
        // TODO: reduce duplication
        try {
            String baseUri = ENDPOINT + PART_REPOSITORY + "/" +  repository.getFullName() + PART_PULL_REQUESTS;

            String url = Uri.parse(baseUri).buildUpon()
                    .appendQueryParameter("access_token", mApiKey)
                    .build().toString();
            String result = getUrl(url);

            JSONArray jsonObjects = new JSONArray(result);

            for (int i = 0; i < jsonObjects.length(); i++) {
                JSONObject pullRequestJSON = jsonObjects.getJSONObject(i);
                PullRequest pullRequest = new PullRequest(pullRequestJSON);
                pullRequest.setRepositoryId(repository.getId());
                pullRequests.add(pullRequest);
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        }

        return pullRequests;
    }

    public ArrayList<Repository> fetchRepositories() throws JSONException {
        ArrayList<Repository> repositories = new ArrayList<Repository>();

        // TODO: Block requests if apikey is not available
        // TODO: reduce duplication
        try {
            String baseUri = ENDPOINT + PART_SUBSCRIPTIONS;

            Integer page = 1;
            Boolean getMore = true;


            while (getMore) {
                String url = Uri.parse(baseUri).buildUpon()
                        .appendQueryParameter("access_token", mApiKey)
                        .appendQueryParameter("page", page.toString())
                        .build().toString();
                String result = getUrl(url);

                JSONArray jsonObjects = new JSONArray(result);

                for (int i = 0; i < jsonObjects.length(); i++) {
                    JSONObject repository = jsonObjects.getJSONObject(i);
                    repositories.add(new Repository(repository));
                }

                if (jsonObjects.length() > 0) {
                    page++;
                } else {
                    getMore = false;
                }
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch repositories: ", ioe);
        }

        return repositories;
    }
}
