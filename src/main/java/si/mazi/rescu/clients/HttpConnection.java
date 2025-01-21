package si.mazi.rescu.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import si.mazi.rescu.HttpMethod;

public interface HttpConnection {

    HttpConnectionType getHttpConnectionType();
    String getHeaderField(String name);
    String getRequestMethod();
    OutputStream getOutputStream() throws IOException;
    int getResponseCode() throws IOException;
    Map<String,List<String>> getHeaderFields();
    InputStream getInputStream() throws IOException;
    InputStream getErrorStream() throws IOException;
    void setRequestMethod(HttpMethod method) throws ProtocolException;
    void setHeader(String key, String value);
    default void doFinalConfig(int contentLength) {}
    void setReadTimeout(int readTimeout);
    void setConnectTimeout(int connTimeout);
    
    boolean isSsl();
    /** TODO: should be made javax.net-independent */
    void setSSLSocketFactory(SSLSocketFactory sslSocketFactory);
    void setHostnameVerifier(HostnameVerifier hostnameVerifier);
    
}
