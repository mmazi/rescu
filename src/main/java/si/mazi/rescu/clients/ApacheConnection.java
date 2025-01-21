package si.mazi.rescu.clients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.mazi.rescu.HttpMethod;

public class ApacheConnection implements HttpConnection {
    
    private static final Logger log = LoggerFactory.getLogger(ApacheConnection.class);
    
    private final String url;
    private final Proxy proxy;
    private final HttpClientBuilder builder = HttpClients.custom();
    private final RequestConfig.Builder requestConfig = RequestConfig.custom();
            
    private boolean executed = false;
    private HttpMethod method = HttpMethod.GET;
    private ByteArrayOutputStream out;
    private final Map<String, String> headers = new HashMap<>();
    
    private CloseableHttpResponse response;

    private ApacheConnection(String url, Proxy proxy) {
        super();
        this.url = url;
        this.proxy = proxy;
    }
    
    public static HttpConnection create(String url, Proxy proxy) throws MalformedURLException, IOException {
        return new ApacheConnection(url, proxy);
    }
    
    private void exec() throws ClientProtocolException, IOException {
        if (executed) {
            return;
        }
        HttpClientBuilder clientBuilder = builder
                .setDefaultRequestConfig(requestConfig.build());
        if (proxy != null && proxy != Proxy.NO_PROXY) {
            InetSocketAddress address = (InetSocketAddress) proxy.address(); 
            HttpHost proxy = new HttpHost(address.getHostName(), address.getPort());
            clientBuilder.setProxy(proxy);
        }
        try ( CloseableHttpClient client = clientBuilder.build() ) {
        
            HttpRequestBase request = createRequest(method, url);
            
            if (request instanceof HttpEntityEnclosingRequestBase && out != null) {
                HttpEntityEnclosingRequestBase req = (HttpEntityEnclosingRequestBase) request;
                HttpEntity entity = req.getEntity();
                if (entity == null) {
                    entity = new ByteArrayEntity(out.toByteArray());
                    req.setEntity(entity);
                }
            }
            headers.forEach(request::addHeader);
            response = client.execute(request);
            executed = true;
        }
    }

    private static HttpRequestBase createRequest(HttpMethod method, String url) {
        switch(method) {
            case GET: return new HttpGet(url); 
            case POST: return new HttpPost(url);
            case PUT: return new HttpPut(url);
            case DELETE: return new HttpDelete(url);
            case HEAD: return new HttpHead(url); 
            case OPTIONS: return new HttpOptions(url);
            case PATCH: return new HttpPatch(url);
            default: throw new RuntimeException("Not supported http method: " + method);
        }
    }

    @Override
    public String getHeaderField(String name) {
        if (!executed) {
            throw new RuntimeException("Request is not executed yet.");
        }
        return response.getFirstHeader(name).getValue();
    }
    
    @Override
    public void setRequestMethod(HttpMethod method) throws ProtocolException {
        this.method = method;
    }

    @Override
    public String getRequestMethod() {
        return method.name();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if(out == null) {
            out = new ByteArrayOutputStream();
        }
        return out;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        exec();
        return response.getEntity().getContent();
    }
    
    @Override
    public InputStream getErrorStream() throws IOException {
        return getInputStream();
    }
    
    @Override
    public int getResponseCode() throws IOException {
        exec();
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        
        Map<String, List<String>> result = new HashMap<>();
        Stream.of(response.getAllHeaders()).forEach(h -> {
            List<String> list = result.get(h.getName());
            if (list == null) {
                list = new ArrayList<>();
                result.put(h.getName(), list);
            }
            list.add(h.getValue());
        });
        
        return result;
    }

    @Override
    public void setHeader(String key, String value) {
        if ("Content-Length".equals(key)) { // need to drop this header here, otherwise we get "Content-Length header already present"
            log.warn("'Content-Length' header already present, ignoring the explicitly set value: '{}'.", value);
            return;
        }
        headers.put(key, value);
    }

    /**
     * @param readTimeout an {@code int} that specifies the timeout value to be used in milliseconds
     */
    @Override
    public void setReadTimeout(int readTimeout) {
        requestConfig.setSocketTimeout(readTimeout);
    }

    /**
     * @param connTimeout an {@code int} that specifies the connect timeout value in milliseconds
     */
    @Override
    public void setConnectTimeout(int connTimeout) {
        requestConfig.setConnectTimeout(connTimeout);
    }

    @Override
    public boolean isSsl() {
        return url.startsWith("https");
    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        if (sslSocketFactory != null) {
            throw new UnsupportedOperationException("Setting a SSLSocketFactory not supported for Apache client");
        }
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        builder.setSSLHostnameVerifier(hostnameVerifier);
    }

    @Override
    public HttpConnectionType getHttpConnectionType() {
        return HttpConnectionType.apache;
    }

}
