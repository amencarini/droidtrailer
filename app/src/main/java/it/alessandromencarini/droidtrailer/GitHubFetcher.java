package it.alessandromencarini.droidtrailer;

import android.net.Uri;
import android.util.Log;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessandromencarini on 18/09/2014.
 */
public class GitHubFetcher {

    private String mApiKey;

    private GitHubClient mGitHubClient = new GitHubClient();
    private PullRequestService mPullRequestService;
    private IssueService mIssueService;
    private RepositoryService mRepositoryService;

    private static final String TAG      = "GitHubFetcher";

    private static final String ENDPOINT = "https://api.github.com";
    private static final String PART_SUBSCRIPTIONS = "/user/subscriptions";

    public GitHubFetcher(String apiKey) {
        mApiKey = apiKey;
        mGitHubClient.setOAuth2Token(apiKey);

        mPullRequestService= new PullRequestService(mGitHubClient);
        mIssueService = new IssueService(mGitHubClient);
        mRepositoryService = new RepositoryService(mGitHubClient);
    }

    public ArrayList<PullRequest> fetchPullRequestsForRepository(Repository repository) throws IOException {
        ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();

        org.eclipse.egit.github.core.Repository remoteRepository = mRepositoryService.getRepository(repository.getOwner(), repository.getName());


        ArrayList<org.eclipse.egit.github.core.PullRequest> pullRequestList = (ArrayList<org.eclipse.egit.github.core.PullRequest>)mPullRequestService.getPullRequests(remoteRepository, "open");

        for (org.eclipse.egit.github.core.PullRequest remotePullRequest : pullRequestList) {
            pullRequests.add(new PullRequest(
                    remotePullRequest.getId(),
                    remotePullRequest.getTitle(),
                    remotePullRequest.getUser().getLogin(),
                    remotePullRequest.getState(),
                    remotePullRequest.getHtmlUrl(),
                    remotePullRequest.getUser().getAvatarUrl(),
                    remotePullRequest.getNumber(),
                    remotePullRequest.getComments(),
                    null,
                    null, // TODO: add check for current API user
                    remotePullRequest.isMergeable(),
                    remotePullRequest.getCreatedAt(),
                    remotePullRequest.getClosedAt(),
                    remotePullRequest.getMergedAt(),
                    null,
                    remoteRepository.getId()
            ));
        }

        return pullRequests;
    }

    public PullRequest updatePullRequest(PullRequest pullRequest) throws IOException {
        Repository localRepository = pullRequest.getRepository();
        org.eclipse.egit.github.core.Repository remoteRepository = mRepositoryService.getRepository(localRepository.getOwner(), localRepository.getName());

        org.eclipse.egit.github.core.PullRequest remotePullRequest = mPullRequestService.getPullRequest(remoteRepository, pullRequest.getNumber());

        pullRequest.setClosedAt(remotePullRequest.getClosedAt());
        pullRequest.setMergedAt(remotePullRequest.getMergedAt());
        pullRequest.setState(remotePullRequest.getState());
        pullRequest.setTitle(remotePullRequest.getTitle());

        return pullRequest;
    }

    public ArrayList<Comment> fetchCommentsForPullRequests(ArrayList<PullRequest> pullRequests) throws IOException {
        ArrayList<Comment> allComments = new ArrayList<Comment>();

        for (PullRequest pullRequest : pullRequests) {
            Repository localRepository = pullRequest.getRepository();
            RepositoryId repo = new RepositoryId(localRepository.getOwner(), localRepository.getName());

            List<org.eclipse.egit.github.core.Comment> remoteGeneralComments = mIssueService.getComments(repo, pullRequest.getNumber());
            remoteGeneralComments.addAll(mPullRequestService.getComments(repo, pullRequest.getNumber()));

            for (org.eclipse.egit.github.core.Comment remoteComment : remoteGeneralComments) {
                Comment comment = new Comment(
                        remoteComment.getId(),
                        remoteComment.getUser().getAvatarUrl(),
                        remoteComment.getBody(),
                        remoteComment.getUser().getLogin(),
                        remoteComment.getUrl(),
                        remoteComment.getCreatedAt(),
                        pullRequest.getId()
                );

                allComments.add(comment);
            }
        }

        return allComments;
    }

    public Response getUrl(String urlSpec) throws IOException {
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

            return new Response(out.toByteArray(), connection.getHeaderFields());
        } finally {
            connection.disconnect();
        }
    }

    public ArrayList<Repository> fetchRepositories() throws JSONException {
        ArrayList<Repository> repositories = new ArrayList<Repository>();

        // TODO: Block requests if apikey is not available
        // TODO: reduce duplication
        try {
            String baseUri = ENDPOINT + PART_SUBSCRIPTIONS;

            String url = Uri.parse(baseUri).buildUpon()
                    .appendQueryParameter("access_token", mApiKey)
                    .build().toString();

            do {
                Response response = getUrl(url);

                JSONArray jsonObjects = new JSONArray(response.getBodyString());

                for (int i = 0; i < jsonObjects.length(); i++) {
                    JSONObject repository = jsonObjects.getJSONObject(i);
                    repositories.add(new Repository(repository));
                }

                List<String> nextHeader = response.getHeaders().get("Link");
                String nextUrl = nextHeader.get(0);
                Pattern pattern = Pattern.compile("<(.*)>; rel=\"next\"");
                Matcher matcher = pattern.matcher(nextUrl);

                if (matcher.find()) {
                    url = matcher.group(1);
                } else {
                    url = null;
                }
            } while (url != null);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch repositories: ", ioe);
        }

        return repositories;
    }

}
