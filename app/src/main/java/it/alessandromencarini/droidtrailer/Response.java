package it.alessandromencarini.droidtrailer;

import java.util.List;
import java.util.Map;

/**
 * Created by ale on 20/10/2014.
 */
public class Response {
    private byte[] mBody;
    private Map<String, List<String>> mHeaders;

    public Response(byte[] body, Map<String, List<String>> headers) {
        mBody = body;
        mHeaders = headers;
    }

    public byte[] getBody() {
        return mBody;
    }

    public String getBodyString() {
        return new String(mBody);
    }

    public Map<String, List<String>> getHeaders() {
        return mHeaders;
    }
}
