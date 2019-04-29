/*
 * Copyright (C) 2015 Matija Mazi
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
 *
 */

package si.mazi.rescu;

import java.net.HttpURLConnection;

/**
 * NOT thread-safe
 */
class TestRestInvocationHandler extends RestInvocationHandler {

    private RestInvocation invocation;
    private final int responseStatusCode;
    private final String responseBody;

    public TestRestInvocationHandler(Class<?> restInterface, ClientConfig config,
                                     String responseBody, int responseStatusCode) {
        this(restInterface, config, responseBody, responseStatusCode, "https://example.com");
    }

    public TestRestInvocationHandler(Class<?> restInterface, ClientConfig config,
                                     String responseBody, int responseStatusCode, String baseUrl) {
        super(restInterface, baseUrl, config);

        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
    }

    @Override
    protected HttpURLConnection invokeHttp(RestInvocation invocation) {
        this.invocation = invocation;
        return null;
    }

    @Override
    protected Object receiveAndMap(RestMethodMetadata methodMetadata, HttpURLConnection connection) throws Exception {
        InvocationResult invocationResult = new InvocationResult(getResponseBody(), getResponseStatusCode());
        return mapInvocationResult(invocationResult, methodMetadata);
    }

    public RestInvocation getInvocation() {
        return invocation;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
