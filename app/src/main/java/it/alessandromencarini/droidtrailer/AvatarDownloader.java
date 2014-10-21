package it.alessandromencarini.droidtrailer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.eclipse.egit.github.core.client.GitHubClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ale on 20/10/2014.
 */
public class AvatarDownloader<Token> extends HandlerThread {
    private static final String TAG = "AvatarDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private GitHubFetcher mClient;

    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());

    Handler mResponseHandler;
    Listener<Token> mListener;

    public interface Listener<Token> {
        void onAvatarDownloaded(Token token, Bitmap avatar);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public AvatarDownloader(GitHubFetcher client, Handler responseHandler) {
        super(TAG);
        this.mClient = client;
        this.mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    Token token = (Token)msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    public void queueAvatar(Token token, String url) {
        requestMap.put(token, url);

        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);
            if (url == null)
                return;

            final Response response = mClient.getUrl(url);
            byte[] bitmapBytes = response.getBody();
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url)
                        return;

                    requestMap.remove(token);
                    mListener.onAvatarDownloaded(token, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
