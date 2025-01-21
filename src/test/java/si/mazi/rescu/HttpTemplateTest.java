/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
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

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import si.mazi.rescu.clients.HttpConnection;
import si.mazi.rescu.clients.HttpConnectionType;
import si.mazi.rescu.clients.JavaConnection;


/**
 * Test class for testing HttpTemplate methods
 */
public class HttpTemplateTest {

    @Test
    public void testGet() throws Exception {
        final HttpURLConnection mockHttpURLConnection = new MockHttpURLConnection("/example-httpdata.txt");
        HttpTemplate testObject = new MockHttpTemplate(JavaConnection.create(mockHttpURLConnection));
        InvocationResult executeResult = executeRequest(testObject, "http://example.com/ticker", null, new HashMap<String, String>(), HttpMethod.GET);
        assertEquals(200, executeResult.getStatusCode());
        assertEquals("Test data", executeResult.getHttpBody());
    }

    @Test
    public void testReadInputStreamAsEncodedString() throws Exception {
        HttpTemplate testObject = new HttpTemplate(30000, null, null, null, null, null, null, HttpConnectionType.DEFAULT) {
            @Override String getResponseEncoding(HttpConnection connection) { return "UTF-8"; }
            @Override boolean izGzipped(HttpConnection connection) { return false; }
        };
        InputStream inputStream = HttpTemplateTest.class.getResourceAsStream("/example-httpdata.txt");
        assertEquals("Test data", testObject.readInputStreamAsEncodedString(inputStream, null));
    }

    @Test
    public void testPostWithError() throws Exception {
        final HttpURLConnection mockHttpURLConnection = new MockErrorHttpURLConnection("/error.json");
        HttpTemplate testObject = new MockHttpTemplate(JavaConnection.create(mockHttpURLConnection));
        InvocationResult executeResult = executeRequest(testObject, "http://example.org/accountinfo", "Example", new HashMap<String, String>(), HttpMethod.POST);
        assertEquals(500, executeResult.getStatusCode());
        assertEquals("{\"result\":\"error\",\"error\":\"Order not found\",\"token\":\"unknown_error\"}", executeResult.getHttpBody());
    }

    //TODO: test sent body data and headers

    /**
     * Requests JSON via an HTTP POST
     *
     * @param urlString   A string representation of a URL
     * @param requestBody The contents of the request body
     * @param httpHeaders Any custom header values (application/json is provided automatically)
     * @param method      Http method (usually GET or POST)
     */
    public static InvocationResult executeRequest(HttpTemplate httpTemplate, String urlString, String requestBody,
                                                  Map<String, String> httpHeaders, HttpMethod method)
            throws IOException {
        return httpTemplate.receive(httpTemplate.send(urlString, requestBody, httpHeaders, method));
    }

    private static class MockHttpTemplate extends HttpTemplate {

        private final HttpConnection mockHttpURLConnection;

        public MockHttpTemplate(HttpConnection mockHttpURLConnection) {
            super(30000, null, null, null, null, null, null, HttpConnectionType.DEFAULT);
            this.mockHttpURLConnection = mockHttpURLConnection;
        }

        @Override
        public HttpConnection getRescuHttpURLConnection(String urlString) throws IOException {
            return mockHttpURLConnection;
        }
    }

    private static class MockHttpURLConnection extends HttpURLConnection {

        protected final String resourcePath;

        public MockHttpURLConnection(String resourcePath) throws MalformedURLException {
            super(new URL("http://example.org"));
            this.resourcePath = resourcePath;
        }

        @Override public void disconnect() { }

        @Override public boolean usingProxy() { return false; }

        @Override public void connect() throws IOException { }

        @Override public int getResponseCode() throws IOException { return 200; }

        @Override public OutputStream getOutputStream() throws IOException { return new ByteArrayOutputStream(); }

        @Override public InputStream getInputStream() throws IOException {
            return HttpTemplateTest.class.getResourceAsStream(resourcePath);
        }
    }

    private static class MockErrorHttpURLConnection extends MockHttpURLConnection {
        public MockErrorHttpURLConnection(String errorResourcePath) throws MalformedURLException {
            super(errorResourcePath);
        }

        @Override public int getResponseCode() throws IOException { return 500; }

        @Override public InputStream getInputStream() throws IOException { return null; }

        @Override
        public InputStream getErrorStream() {
            return HttpTemplateTest.class.getResourceAsStream(resourcePath);
        }
    }
}
