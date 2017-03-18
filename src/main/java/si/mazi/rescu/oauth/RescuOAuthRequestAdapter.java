package si.mazi.rescu.oauth;

import oauth.signpost.basic.HttpURLConnectionRequestAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author Rafał Krupiński
 */
public class RescuOAuthRequestAdapter extends HttpURLConnectionRequestAdapter {
    private final String messagePayload;

    public RescuOAuthRequestAdapter(HttpURLConnection connection, String messagePayload) {
        super(connection);
        this.messagePayload = messagePayload;
    }

    @Override
    public InputStream getMessagePayload() throws IOException {
        return messagePayload != null ? new ByteArrayInputStream(messagePayload.getBytes("UTF-8")) : null;
    }
}
