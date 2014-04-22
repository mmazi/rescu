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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * Test class for testing HttpTemplate methods
 */
public class HttpTemplateTest {

    @Test
    public void testGet() throws Exception {
        final HttpURLConnection mockHttpURLConnection = new MockHttpURLConnection("/example-httpdata.txt");
        HttpTemplate testObject = new MockHttpTemplate(mockHttpURLConnection);
        InvocationResult executeResult = testObject.executeRequest("http://example.com/ticker", null, new HashMap<String, String>(), HttpMethod.GET, null);
        assertEquals(200, executeResult.getStatusCode());
        assertEquals("Test data", executeResult.getHttpBody());
    }

    @Test
    public void testReadInputStreamAsEncodedString() throws Exception {
        HttpTemplate testObject = new HttpTemplate(30000, null, null, null, null) {
            @Override String getResponseEncoding(URLConnection connection) { return "UTF-8"; }
            @Override boolean izGzipped(HttpURLConnection connection) { return false; }
        };
        InputStream inputStream = HttpTemplateTest.class.getResourceAsStream("/example-httpdata.txt");
        assertEquals("Test data", testObject.readInputStreamAsEncodedString(inputStream, null));
    }

    @Test
    public void testPostWithError() throws Exception {
        final HttpURLConnection mockHttpURLConnection = new MockErrorHttpURLConnection("/error.json");
        HttpTemplate testObject = new MockHttpTemplate(mockHttpURLConnection);
        InvocationResult executeResult = testObject.executeRequest("http://example.org/accountinfo", "Example", new HashMap<String, String>(), HttpMethod.POST, null);
        assertEquals(500, executeResult.getStatusCode());
        assertEquals("{\"result\":\"error\",\"error\":\"Order not found\",\"token\":\"unknown_error\"}", executeResult.getHttpBody());
    }
    
    //TODO: test sent body data and headers

    private static class MockHttpTemplate extends HttpTemplate {

        private final HttpURLConnection mockHttpURLConnection;

        public MockHttpTemplate(HttpURLConnection mockHttpURLConnection) {
            super(30000, null, null, null, null);
            this.mockHttpURLConnection = mockHttpURLConnection;
        }

        @Override
        public HttpURLConnection getHttpURLConnection(String urlString) throws IOException {
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
