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

import si.mazi.rescu.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.utils.AssertUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
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
    private final Map<String, String> defaultHttpHeaders = new HashMap<String, String>();
    private final int readTimeout;
    private final Proxy proxy;
    private final SSLSocketFactory sslSocketFactory;
    private final HostnameVerifier hostnameVerifier;

    /**
     * Constructor
     */
    public HttpTemplate(int readTimeout, String proxyHost, Integer proxyPort,
            SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
        this.readTimeout = readTimeout;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;

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

    /**
     * Requests JSON via an HTTP POST
     *
     * @param urlString   A string representation of a URL
     * @param returnType  The required return type
     * @param requestBody The contents of the request body
     * @param httpHeaders Any custom header values (application/json is provided automatically)
     * @param method      Http method (usually GET or POST)
     * @param contentType the mime type to be set as the value of the Content-Type header
     * @param exceptionType
     * @return String - the fetched JSON String
     */
    public InvocationResult executeRequest(String urlString, String requestBody,
            Map<String, String> httpHeaders, HttpMethod method, String contentType)
            throws IOException {

        log.debug("Executing {} request at {}", method, urlString);
        log.trace("Request body = {}", requestBody);
        log.trace("Request headers = {}", httpHeaders);

        AssertUtil.notNull(urlString, "urlString cannot be null");
        AssertUtil.notNull(httpHeaders, "httpHeaders should not be null");

        if (contentType != null) {
            httpHeaders.put("Content-Type", contentType);
        }

        int contentLength = requestBody == null ? 0 : requestBody.length();
        HttpURLConnection connection = configureURLConnection(method, urlString, httpHeaders, contentLength);

        if (contentLength > 0) {
            // Write the request body
            connection.getOutputStream().write(requestBody.getBytes(CHARSET_UTF_8));
        }

        int httpStatus = connection.getResponseCode();
        log.debug("Request http status = {}", httpStatus);

        InputStream inputStream = !HttpUtils.isErrorStatusCode(httpStatus) ?
            connection.getInputStream() : connection.getErrorStream();
        String responseString = readInputStreamAsEncodedString(inputStream, connection);
        log.trace("Http call returned {}; response body:\n{}", httpStatus, responseString);
        
        return new InvocationResult(responseString, httpStatus);
    }

    /**
     * Provides an internal convenience method to allow easy overriding by test classes
     *
     * @param method        The HTTP method (e.g. GET, POST etc)
     * @param urlString     A string representation of a URL
     * @param httpHeaders   The HTTP headers (will override the defaults)
     * @param contentLength
     * @return An HttpURLConnection based on the given parameters
     * @throws IOException If something goes wrong
     */
    private HttpURLConnection configureURLConnection(HttpMethod method, String urlString, Map<String, String> httpHeaders, int contentLength) throws IOException {

        AssertUtil.notNull(method, "method cannot be null");
        AssertUtil.notNull(urlString, "urlString cannot be null");
        AssertUtil.notNull(httpHeaders, "httpHeaders cannot be null");

        HttpURLConnection connection = getHttpURLConnection(urlString);
        connection.setRequestMethod(method.name());

        // Copy default HTTP headers
        Map<String, String> headerKeyValues = new HashMap<String, String>(defaultHttpHeaders);

        // Merge defaultHttpHeaders with httpHeaders
        headerKeyValues.putAll(httpHeaders);

        // Add HTTP headers to the request
        for (Map.Entry<String, String> entry : headerKeyValues.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
            log.trace("Header request property: key='{}', value='{}'", entry.getKey(), entry.getValue());
        }

        // Perform additional configuration for POST
        if (contentLength > 0) {
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Add content length to header
            connection.setRequestProperty("Content-Length", Integer.toString(contentLength));
        }

        return connection;
    }

    /**
     * @param urlString
     * @return a HttpURLConnection instance
     * @throws IOException
     */
    protected HttpURLConnection getHttpURLConnection(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection(proxy);
        
        if (readTimeout > 0) {
            connection.setReadTimeout(readTimeout);
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
            final InputStreamReader in = responseEncoding != null ? new InputStreamReader(inputStream, responseEncoding) : new InputStreamReader(inputStream);
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

}
