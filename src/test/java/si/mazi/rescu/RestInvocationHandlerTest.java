/**
 * Copyright (C) 2013 Matija Mazi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
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

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import si.mazi.rescu.dto.DummyAccountInfo;
import si.mazi.rescu.dto.DummyTicker;
import si.mazi.rescu.dto.GenericResult;
import si.mazi.rescu.dto.Order;

/**
 * @author Matija Mazi
 */
public class RestInvocationHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(RestInvocationHandlerTest.class);

    @Test
    public void testInvocationData() throws Exception {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.add(PathParam.class, "version", 2);
        clientConfig.add(HeaderParam.class, "testHeader", "lorem");

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.buy("john", "secret", new BigDecimal("3.14"), new BigDecimal("10.00"));
        assertRequestData(testHandler, Order.class, null, "https://example.com/api/2/buy/", HttpMethod.POST, "https://example.com", "api/2/buy/", "buy/", "", "user=john&password=secret&amount=3.14&price=10.00", FormParam.class, "user", "john");
        assertEquals("lorem", testHandler.invocation.getHttpHeaders().get("testHeader"));
        
        BigDecimal amount = new BigDecimal("3.14");
        proxy.buy("john", "secret", amount, null);
        assertRequestData(testHandler, Order.class, null, "https://example.com/api/2/buy/", HttpMethod.POST, "https://example.com", "api/2/buy/", "buy/", "", "user=john&password=secret&amount=3.14", FormParam.class, "amount", amount);

        proxy.withdrawBitcoin("john", "secret", amount, "mybitcoinaddress");
        assertRequestData(testHandler, Object.class, null, "https://example.com/api/2/bitcoin_withdrawal/john?amount=3.14&address=mybitcoinaddress", HttpMethod.POST, "https://example.com", "api/2/bitcoin_withdrawal/john", "bitcoin_withdrawal/john", "amount=3.14&address=mybitcoinaddress", "password=secret", QueryParam.class, "amount", amount);

        proxy.getTicker("btc", "usd");
        assertRequestData(testHandler, DummyTicker.class, null, "https://example.com/api/2/btc_usd/ticker", HttpMethod.GET, "https://example.com", "api/2/btc_usd/ticker", "btc_usd/ticker", "", "", PathParam.class, "ident", "btc");

        proxy.getInfo(1000L, 2000L);
        assertRequestData(testHandler, Object.class, null, "https://example.com/api/2", HttpMethod.POST, "https://example.com", "api/2", "", "", "method=getInfo", FormParam.class, "method", "getInfo");
    }

    @Test
    public void testHttpBasicAuth() throws Exception {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.add(PathParam.class, "version", 0);

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        BasicAuthCredentials credentials = new BasicAuthCredentials("Aladdin", "open sesame");
        proxy.testBasicAuth(credentials, 23);
        HashMap<String, String> authHeaders = new HashMap<String, String>();
        authHeaders.put("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        assertRequestData(testHandler, Object.class, authHeaders, "https://example.com/api/0/auth?param=23", HttpMethod.GET, "https://example.com", "api/0/auth", "auth", "param=23", null);
    }

    @Test
    public void testHttpBasicAuthWithConfig() throws Exception {
        ClientConfig config = ClientConfigUtil.addBasicAuthCredentials(new ClientConfig(), "Aladdin", "open sesame");
        config.add(PathParam.class, "version", 2);

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, config, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.getInfo(2L, 5L);
        HashMap<String, String> authHeaders = new HashMap<String, String>();
        authHeaders.put("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        assertRequestData(testHandler, Object.class, authHeaders, "https://example.com/api/2", HttpMethod.POST, "https://example.com", "api/2", "", "", null);
    }

    private void assertRequestData(TestRestInvocationHandler testHandler, Class resultClass, Map<String, String> headers, String url, HttpMethod httpMethod, String baseUrl, String path, String methodPath, String queryString, String postBody) {
        assertRequestData(testHandler, resultClass, headers, url, httpMethod, baseUrl, path, methodPath, queryString, postBody, null, null, null);
    }

    private void assertRequestData(TestRestInvocationHandler testHandler, Class resultClass, Map<String, String> headers, String url, HttpMethod httpMethod, String baseUrl, String path, String methodPath, String queryString, String postBody, Class<? extends Annotation> paramAnn, String paramName, Object expectedParamValue) {
        Assert.assertEquals(testHandler.invocation.getInvocationUrl(), url);
        Assert.assertEquals(testHandler.invocation.getMethodPath(), methodPath);
        Assert.assertEquals(testHandler.invocation.getBaseUrl(), baseUrl);
        Assert.assertEquals(testHandler.invocation.getQueryString(), queryString);
        Assert.assertEquals(testHandler.invocation.getPath(), path);
        Assert.assertEquals(testHandler.invocation.getMethodMetadata().getHttpMethod(), httpMethod);
        Assert.assertEquals(testHandler.invocation.getMethodMetadata().getReturnType(), resultClass);

        if (paramAnn != null) {
            Map<Pair<Class<? extends Annotation>, String>, Object> arguments = ImmutableMap.<Pair<Class<? extends Annotation>, String>, Object>builder().put(new Pair<Class<? extends Annotation>, String>(paramAnn, paramName), expectedParamValue).build();
            for (Pair<Class<? extends Annotation>, String> param : arguments.keySet()) {
                Object argValue = testHandler.invocation.getParamValue(param.first(), param.second());
                Assert.assertEquals(argValue, arguments.get(param), "Wrong param value for " + param + ": " + argValue);
            }
        }
        if (postBody != null) {
            Assert.assertEquals(testHandler.invocation.getRequestBody(), postBody);
        }
        if (headers != null) {
            Assert.assertEquals(headers, testHandler.invocation.getHttpHeaders());
        }
    }

    @Test
    public void testJsonBody() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, null, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testJsonBody(new DummyAccountInfo("mm", "USD", 3));
        Assert.assertEquals(testHandler.invocation.getRequestBody(), "{\"username\":\"mm\",\"currency\":\"USD\",\"amount_int\":3}");
    }

    @Test
    public void testRootPathService() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(RootPathService.class, null, null, 200);
        RootPathService proxy = RestProxyFactory.createProxy(RootPathService.class, testHandler);

        proxy.cancel("424");
        assertRequestData(testHandler, Double.class, null, "https://example.com/cancel?id=424", HttpMethod.DELETE, "https://example.com", "/cancel", "cancel", "id=424", null, QueryParam.class, "id", "424");
    }

    @Test
    public void testCheckedException() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, null, null, 200) {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw new IOException("A simulated I/O problem.");
            }
        };
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        try {
            proxy.io();
            assert false : "Expected an IOException.";
        } catch (IOException expected) {
            log.info("Got expected exception: " + expected);
        }
    }
    
    @Test
    public void testJsonResponse() throws IOException {
        ClientConfig clientConfig = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig,
                ResourceUtils.getResourceAsString("/example-ticker.json"), 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        DummyTicker ticker = proxy.getTicker("BTC", "USD");
        assertEquals(12345, ticker.getLast());
        assertEquals(34567, ticker.getVolume());
    }
    
    @Test
    public void testGenericJsonResponse() throws IOException {
        ClientConfig clientConfig = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig,
                ResourceUtils.getResourceAsString("/example-generic.json"), 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        GenericResult<DummyTicker[]> generic = proxy.getGeneric();
        assertEquals(2, generic.getResult().length);
        assertEquals(12345, generic.getResult()[0].getLast());
        assertEquals(8910, generic.getResult()[1].getVolume());
    }
    
    private static class TestRestInvocationHandler extends RestInvocationHandler {

        private RestInvocation invocation;
        private final int responseStatusCode;
        private final String responseBody;
        
        public TestRestInvocationHandler(Class<?> restInterface, ClientConfig config,
                String responseBody, int responseStatusCode) {
            super(restInterface, "https://example.com", config);
            
            this.responseStatusCode = responseStatusCode;
            this.responseBody = responseBody;
        }

        @Override
        protected InvocationResult invokeHttp(RestInvocation invocation) {
            this.invocation = invocation;
            return new InvocationResult(responseBody, responseStatusCode);
        }

    }
}
