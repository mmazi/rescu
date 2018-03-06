/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 * Copyright (C) 2012 - 2013 Matija Mazi matija.mazi@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package si.mazi.rescu;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.oauth.RescuOAuthRequestAdapter;
import si.mazi.rescu.utils.HttpUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Various HTTP utility methods
 */
class HttpTemplate {

    public final static String CHARSET_UTF_8 = "UTF-8";

    private final Logger log = LoggerFactory.getLogger(HttpTemplate.class);

    /**
     * Default request header fields
     */
    private final Map<String, String> defaultHttpHeaders = new HashMap<>();
    private final int connTimeout;
    private final int readTimeout;
    private final Proxy proxy;
    private final SSLSocketFactory sslSocketFactory;
    private final HostnameVerifier hostnameVerifier;
    private final OAuthConsumer oAuthConsumer;


    HttpTemplate(int readTimeout, String proxyHost, Integer proxyPort,
                 SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier, OAuthConsumer oAuthConsumer) {
      this(0, readTimeout, proxyHost, proxyPort, sslSocketFactory, hostnameVerifier, oAuthConsumer);
    }
    
    HttpTemplate(int connTimeout, int readTimeout, String proxyHost, Integer proxyPort,
                 SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier, OAuthConsumer oAuthConsumer) {
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.oAuthConsumer = oAuthConsumer;

        defaultHttpHeaders.put("Accept-Charset", CHARSET_UTF_8);
        // defaultHttpHeaders.put("Content-Type", "application/x-www-form-urlencoded");
        defaultHttpHeaders.put("Accept", "application/json");
        // User agent provides statistics for servers, but some use it for content negotiation so fake good agents
        defaultHttpHeaders.put("User-Agent", "ResCU JDK/6 AppleWebKit/535.7 Chrome/16.0.912.36 Safari/535.7"); // custom User-Agent

        if (proxyHost == null || proxyPort == null) {
            proxy = Proxy.NO_PROXY;
        } else {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            log.info("Using proxy {}", proxy);
        }
    }

    HttpURLConnection send(String urlString, String requestBody, Map<String, String> httpHeaders, HttpMethod method) throws IOException {
        log.debug("Executing {} request at {}", method, urlString);
        log.trace("Request body = {}", requestBody);
        log.trace("Request headers = {}", httpHeaders);

        preconditionNotNull(urlString, "urlString cannot be null");
        preconditionNotNull(httpHeaders, "httpHeaders should not be null");

        int contentLength = requestBody == null ? 0 : requestBody.getBytes().length;
        HttpURLConnection connection = configureURLConnection(method, urlString, httpHeaders, contentLength);

        if (oAuthConsumer != null) {
            HttpRequest request = new RescuOAuthRequestAdapter(connection, requestBody);

            try {
                oAuthConsumer.sign(request);
            } catch (OAuthException e) {
                throw new RuntimeException("OAuth error", e);
            }
        }

        if (contentLength > 0) {
            // Write the request body
            OutputStream out = connection.getOutputStream();
            out.write(requestBody.getBytes(CHARSET_UTF_8));
            out.flush();
        }
        return connection;
    }

    InvocationResult receive(HttpURLConnection connection) throws IOException {
        int httpStatus = connection.getResponseCode();
        log.debug("Request http status = {}", httpStatus);
        if (log.isTraceEnabled()) {
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null) {
                    log.trace("Header response property: key='{}', value='{}'", entry.getKey(), entry.getValue());
                }
            }
        }

        InputStream inputStream = !HttpUtils.isErrorStatusCode(httpStatus) ?
            connection.getInputStream() : connection.getErrorStream();
        String responseString = readInputStreamAsEncodedString(inputStream, connection);
        if (responseString != null && responseString.startsWith("\uFEFF")) {
            responseString = responseString.substring(1);
        }
        log.trace("Http call returned {}; response body:\n{}", httpStatus, responseString);

        return new InvocationResult(responseString, httpStatus);
    }

    /**
     * Provides an internal convenience method to allow easy overriding by test classes
     *
     * @param method        The HTTP method (e.g. GET, POST etc)
     * @param urlString     A string representation of a URL
     * @param httpHeaders   The HTTP headers (will override the defaults)
     * @param contentLength The Content-Length request property
     * @return An HttpURLConnection based on the given parameters
     * @throws IOException If something goes wrong
     */
    private HttpURLConnection configureURLConnection(HttpMethod method, String urlString, Map<String, String> httpHeaders, int contentLength) throws IOException {

        preconditionNotNull(method, "method cannot be null");
        preconditionNotNull(urlString, "urlString cannot be null");
        preconditionNotNull(httpHeaders, "httpHeaders cannot be null");

        HttpURLConnection connection = getHttpURLConnection(urlString);
        connection.setRequestMethod(method.name());

        Map<String, String> headerKeyValues = new HashMap<>(defaultHttpHeaders);

        headerKeyValues.putAll(httpHeaders);

        for (Map.Entry<String, String> entry : headerKeyValues.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
            log.trace("Header request property: key='{}', value='{}'", entry.getKey(), entry.getValue());
        }

        // Perform additional configuration for POST
        if (contentLength > 0) {
            connection.setDoOutput(true);
            connection.setDoInput(true);
        }
        connection.setRequestProperty("Content-Length", Integer.toString(contentLength));

        return connection;
    }

    protected HttpURLConnection getHttpURLConnection(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection(proxy);

        if (readTimeout > 0) {
            connection.setReadTimeout(readTimeout);
        }
        if (connTimeout > 0) {
          connection.setConnectTimeout(connTimeout);
        }
        
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
            
            if (sslSocketFactory != null) {
                httpsConnection.setSSLSocketFactory(sslSocketFactory);
            }
            
            if (hostnameVerifier != null) {
                httpsConnection.setHostnameVerifier(hostnameVerifier);
            }
        }
        
        return connection;
    }

    /**
     * <p>
     * Reads an InputStream as a String allowing for different encoding types. This closes the stream at the end.
     * </p>
     *
     * @param inputStream      The input stream
     * @param connection     The HTTP connection object
     * @return A String representation of the input stream
     * @throws IOException If something goes wrong
     */
    String readInputStreamAsEncodedString(InputStream inputStream, HttpURLConnection connection) throws IOException {
        if (inputStream == null) {
            return null;
        }

        BufferedReader reader = null;
        try {
            String responseEncoding = getResponseEncoding(connection);
            if (izGzipped(connection)) {
                inputStream = new GZIPInputStream(inputStream);
            }
            final InputStreamReader in = responseEncoding != null ? new InputStreamReader(inputStream, responseEncoding)
                    : new InputStreamReader(inputStream, CHARSET_UTF_8);
            reader = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            inputStream.close();
            if (reader != null) {
                try { reader.close(); } catch (IOException ignore) { }
            }
        }
    }

    boolean izGzipped(HttpURLConnection connection) {
        return "gzip".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
    }

    /**
     * Determine the response encoding if specified
     *
     * @param connection The HTTP connection
     * @return The response encoding as a string (taken from "Content-Type")
     */
    String getResponseEncoding(URLConnection connection) {

        String charset = null;

        String contentType = connection.getHeaderField("Content-Type");
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    charset = param.split("=", 2)[1];
                    break;
                }
            }
        }
        return charset;
    }

    private static void preconditionNotNull(Object what, String message) {
        if (what == null) {
            throw new NullPointerException(message);
        }
    }

}
