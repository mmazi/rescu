package si.mazi.rescu.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import si.mazi.rescu.HttpMethod;

public class JavaConnection implements HttpConnection {
    
    private final HttpURLConnection connection;

    private JavaConnection(HttpURLConnection connection) {
        super();
        this.connection = connection;
    }
    
    public static HttpConnection create(String urlString, Proxy proxy) throws MalformedURLException, IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(urlString).openConnection(proxy);
        return create(c);
    }
    public static HttpConnection create(HttpURLConnection c) {
        return new JavaConnection(c);
    }
    
    public HttpURLConnection getHttpURLConnection() {
        return connection;
    }

    @Override
    public String getHeaderField(String name) {
        return connection.getHeaderField(name);
    }

    @Override
    public String getRequestMethod() {
        return connection.getRequestMethod();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    @Override
    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return connection.getHeaderFields();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return connection.getErrorStream();
    }

    @Override
    public void setRequestMethod(HttpMethod method) throws ProtocolException {
        connection.setRequestMethod(method.name());
    }

    @Override
    public void setHeader(String key, String value) {
        connection.setRequestProperty(key, value);
    }
    
    @Override
    public void doFinalConfig(int contentLength) {
        if (contentLength > 0) {
            connection.setDoOutput(true);
            connection.setDoInput(true);
        }
    }

    @Override
    /**
     * @param timeout an {@code int} that specifies the timeout value to be used in milliseconds
     */
    public void setReadTimeout(int readTimeout) {
        connection.setReadTimeout(readTimeout);
    }

    @Override
    /**
     * @param timeout an {@code int} that specifies the connect timeout value in milliseconds
     */
    public void setConnectTimeout(int connTimeout) {
        connection.setConnectTimeout(connTimeout);
    }

    @Override
    public boolean isSsl() {
        return connection instanceof HttpsURLConnection;
    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
        httpsConnection.setSSLSocketFactory(sslSocketFactory);
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
        httpsConnection.setHostnameVerifier(hostnameVerifier);
    }

    @Override
    public HttpConnectionType getHttpConnectionType() {
        return HttpConnectionType.java;
    }
}
