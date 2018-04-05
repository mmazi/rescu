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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import si.mazi.rescu.dto.DummyAccountInfo;
import si.mazi.rescu.dto.DummyTicker;
import si.mazi.rescu.dto.GenericResult;
import si.mazi.rescu.dto.Order;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Matija Mazi
 */
@SuppressWarnings("Duplicates")
public class RestInvocationHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(RestInvocationHandlerTest.class);

    @Test
    public void testInvocationData() throws Exception {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.addDefaultParam(PathParam.class, "version", 2);
        clientConfig.addDefaultParam(HeaderParam.class, "testHeader", "lorem");

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.buy("john", "secret", new BigDecimal("3.14"), new BigDecimal("10.00"));
        assertRequestData(testHandler, Order.class, null, "https://example.com/api/2/buy/", HttpMethod.POST, "https://example.com", "api/2/buy/", "buy/", "", "user=john&password=secret&amount=3.14&price=10.00", FormParam.class, "user", "john");
        assertThat(testHandler.getInvocation().getAllHttpHeaders().get("testHeader")).isEqualTo("lorem");

        BigDecimal amount = new BigDecimal("3.14");
        proxy.buy("john", "secret", amount, null);
        assertRequestData(testHandler, Order.class, null, "https://example.com/api/2/buy/", HttpMethod.POST, "https://example.com", "api/2/buy/", "buy/", "", "user=john&password=secret&amount=3.14", FormParam.class, "amount", amount);

        proxy.withdrawBitcoin("john", "secret", amount, "mybitcoinaddress");
        assertRequestData(testHandler, Object.class, null, "https://example.com/api/2/bitcoin_withdrawal/john?amount=3.14&address=mybitcoinaddress", HttpMethod.POST, "https://example.com", "api/2/bitcoin_withdrawal/john", "bitcoin_withdrawal/john", "amount=3.14&address=mybitcoinaddress", "password=secret", QueryParam.class, "amount", amount);

        proxy.getTicker("btc", "usd");
        assertRequestData(testHandler, DummyTicker.class, null, "https://example.com/api/2/btc_usd/ticker", HttpMethod.GET, "https://example.com", "api/2/btc_usd/ticker", "btc_usd/ticker", "", "", PathParam.class, "ident", "btc");

        proxy.getInfo(1000L, 2000L);
        assertRequestData(testHandler, DummyTicker.class, null, "https://example.com/api/2", HttpMethod.POST, "https://example.com", "api/2", "", "", "method=getInfo", FormParam.class, "method", "getInfo");
    }

    @Test
    public void testEmptyServicePath() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService2.class, new ClientConfig(), null, 200, "https://example.com/api/v2");
        ExampleService2 proxy = RestProxyFactory.createProxy(ExampleService2.class, testHandler);

        proxy.buy("john");
        assertRequestData(testHandler, Order.class, null, "https://example.com/api/v2/buy?user=john", HttpMethod.GET, "https://example.com/api/v2", "/buy", "buy", "user=john", null);
    }

    @Test
    public void testHttpBasicAuth() throws Exception {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.addDefaultParam(PathParam.class, "version", 0);

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        BasicAuthCredentials credentials = new BasicAuthCredentials("Aladdin", "open sesame");
        proxy.testBasicAuth(credentials, 23);
        HashMap<String, String> authHeaders = new HashMap<>();
        authHeaders.put("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        assertRequestData(testHandler, Object.class, authHeaders, "https://example.com/api/0/auth?param=23", HttpMethod.GET, "https://example.com", "api/0/auth", "auth", "param=23", null);
    }

    @Test
    public void testHttpBasicAuthWithConfig() throws Exception {
        ClientConfig config = ClientConfigUtil.addBasicAuthCredentials(new ClientConfig(), "Aladdin", "open sesame");
        config.addDefaultParam(PathParam.class, "version", 2);

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, config, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.getInfo(2L, 5L);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        assertRequestData(testHandler, DummyTicker.class, headers, "https://example.com/api/2", HttpMethod.POST, "https://example.com", "api/2", "", "", null);
    }

    private void assertRequestData(TestRestInvocationHandler testHandler, Class resultClass, Map<String, String> headers, String url, HttpMethod httpMethod, String baseUrl, String path, String methodPath, String queryString, String postBody) {
        assertRequestData(testHandler, resultClass, headers, url, httpMethod, baseUrl, path, methodPath, queryString, postBody, null, null, null);
    }

    private void assertRequestData(TestRestInvocationHandler testHandler, Class resultClass, Map<String, String> headers, String url, HttpMethod httpMethod, String baseUrl, String path, String methodPath, String queryString, String postBody, Class<? extends Annotation> paramAnn, String paramName, Object expectedParamValue) {
        Assert.assertEquals(testHandler.getInvocation().getInvocationUrl(), url);
        Assert.assertEquals(testHandler.getInvocation().getMethodPath(), methodPath);
        Assert.assertEquals(testHandler.getInvocation().getBaseUrl(), baseUrl);
        Assert.assertEquals(testHandler.getInvocation().getQueryString(), queryString);
        Assert.assertEquals(testHandler.getInvocation().getPath(), path);
        Assert.assertEquals(testHandler.getInvocation().getMethodMetadata().getHttpMethod(), httpMethod);
        Assert.assertEquals(testHandler.getInvocation().getMethodMetadata().getReturnType(), resultClass);

        if (paramAnn != null) {
            Map<Pair<Class<? extends Annotation>, String>, Object> arguments = ImmutableMap.<Pair<Class<? extends Annotation>, String>, Object>builder().put(new Pair<Class<? extends Annotation>, String>(paramAnn, paramName), expectedParamValue).build();
            for (Pair<Class<? extends Annotation>, String> param : arguments.keySet()) {
                Object argValue = testHandler.getInvocation().getParamValue(param.first(), param.second());
                Assert.assertEquals(argValue, arguments.get(param), "Wrong param value for " + param + ": " + argValue);
            }
        }
        if (postBody != null) {
            Assert.assertEquals(testHandler.getInvocation().getRequestBody(), postBody);
        }
        if (headers != null) {
            Assert.assertEquals(headers, testHandler.getInvocation().getAllHttpHeaders());
        }
    }

    @Test
    public void testJsonBody() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, null, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testJsonBody(new DummyAccountInfo("mm", "USD", 3));
        Assert.assertEquals(testHandler.getInvocation().getRequestBody(), "{\"username\":\"mm\",\"currency\":\"USD\",\"amount_int\":3}");
    }

    @Test
    public void testRootPathService() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(RootPathService.class, null, null, 200);
        RootPathService proxy = RestProxyFactory.createProxy(RootPathService.class, testHandler);

        proxy.cancel("424");
        assertRequestData(testHandler, Double.class, null, "https://example.com/cancel?id=424", HttpMethod.DELETE, "https://example.com", "/cancel", "cancel", "id=424", null, QueryParam.class, "id", "424");
    }

    @Test
    public void testIOException() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, null, null, 200) {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw new IOException("A simulated I/O problem.");
            }
        };
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        assertThat(catchThrowable(proxy::io))
                .isInstanceOf(IOException.class)
                .hasMessage("A simulated I/O problem.");
    }

    @Test
    public void testCheckedException() throws Exception {
        final String responseBody = "{\"result\":\"error\",\"error\":\"Checked exception error message\", \"myProperty\":\"Property value\"}";
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, null, responseBody, 500);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        final Throwable caught = catchThrowable(proxy::throwsCheckedException);
        assertThat(caught)
                .isInstanceOf(ExampleCheckedException.class);
        assertThat(((ExampleCheckedException) caught).getMyProperty()).isEqualTo("Property value");
    }

    @Test
    public void testJsonResponse() throws IOException {
        ClientConfig clientConfig = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig,
                ResourceUtils.getResourceAsString("/example-ticker.json"), 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        DummyTicker ticker = proxy.getTicker("BTC", "USD");
        assertThat(ticker.getLast()).isEqualTo(12345L);
        assertThat(ticker.getVolume()).isEqualTo(34567L);
    }
    
    @Test
    public void testGenericJsonResponse() throws IOException {
        ClientConfig clientConfig = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig,
                ResourceUtils.getResourceAsString("/example-generic.json"), 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        GenericResult<DummyTicker[]> generic = proxy.getGeneric();
        assertThat(generic.getResult().length).isEqualTo(2);
        assertThat(generic.getResult()[0].getLast()).isEqualTo(12345);
        assertThat(generic.getResult()[1].getVolume()).isEqualTo(8910);
    }

    @Test
    public void testGetTextPlain() throws Exception {
        ClientConfig clientConfig = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, clientConfig, "Hello World in plain text!", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        final String string = proxy.getString();
        assertThat(string).isEqualTo("Hello World in plain text!");
        @SuppressWarnings("deprecation")
        final Map<String, String> httpHeaders = testHandler.getInvocation().getAllHttpHeaders();
        assertThat(httpHeaders.get("Content-Type")).isEqualTo(null);
        assertThat(httpHeaders.get("Accept")).isEqualTo(MediaType.TEXT_PLAIN);
    }

    @Test
    public void testGetTextPlainError() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "Error message.", 400);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        assertThat(catchThrowable(proxy::getString))
                .isInstanceOf(MessageException.class)
                .hasMessage("Error message.");
    }

    @Test
    public void testPutTextPlain() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "OK", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        proxy.putNumber(123456);
        assertThat(testHandler.getInvocation().getRequestBody()).isEqualTo("123456");
    }

    @Test
    public void testPatchTextPlain() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "OK", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        proxy.updateNumber(123456);
        assertThat(testHandler.getInvocation().getRequestBody()).isEqualTo("123456");
    }

    @Test
    public void testValueGenerator()  {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "OK", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        proxy.getNonce(new ConstantValueFactory<>(1L));
    }

    @Test
    public void testSciNotUrl() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "OK", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        final String numberString = "0.00000043";
        final BigDecimal smallNumber = new BigDecimal(numberString);
        Assert.assertTrue(smallNumber.toString().toUpperCase().contains("E"));

        proxy.testSmallNumbersQuery(smallNumber);

        final String invocationUrl = testHandler.getInvocation().getInvocationUrl();
        Assert.assertTrue(invocationUrl.contains(numberString), invocationUrl);
    }

    @Test
    public void testSciNotJson() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "OK", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        final String numberString = "0.00000043";
        final BigDecimal smallNumber = new BigDecimal(numberString);
        Assert.assertTrue(smallNumber.toString().toUpperCase().contains("E"));

        proxy.testSmallNumbersJson(smallNumber);

        final String requestBody = testHandler.getInvocation().getRequestBody();
        Assert.assertTrue(requestBody.contains(numberString), requestBody);
    }

    @Test
    public void testParseAsExceptionWhenReturnTypeParseFails() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), ResourceUtils.getResourceAsString("/error.json"), 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        ExampleException caughtException = ExceptionUtils.catchException(ExampleException.class, () -> proxy.getInfo(0L, 10L));
        assertThat(caughtException.getError()).isEqualTo("Order not found");
    }

    @Test
    public void testParseAsExceptionWhenHttpErrorAndNoExceptionDeclared() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), null, 500);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        assertThat(catchThrowable(() -> proxy.testIOExceptionDeclared(null)))
                .isInstanceOf(HttpStatusIOException.class)
                .hasMessage("HTTP status code was not OK: 500");
    }

    @Test
    public void testParseAsExceptionWhenReturnTypeParseFailsAndNoExceptionDeclared() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{\"result\":\"error\",\"error\":\"Not parsable as ticker\"}", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        assertThat(catchThrowable(proxy::testIOExceptionDeclared))
                .isInstanceOf(HttpStatusIOException.class)
                .hasMessage(String.format("Response body could not be parsed as method return type %s: last and volume required", DummyTicker.class.toString()));
    }

    @Test
    public void testExceptionOnArrayMethod() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "[{\"last\":300,\"volume\":1}]", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        DummyTicker[] result = proxy.testExceptionOnArrayMethod("");
        Assert.assertEquals(result.length, 1);

        testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{\"result\":\"error\",\"error\":\"Not good\"}", 200);
        ExampleService proxy1 = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        final ExampleException caughtException = ExceptionUtils.catchException(ExampleException.class, () -> proxy1.testExceptionOnArrayMethod(""));
        assertThat(caughtException.getError()).isEqualTo("Not good");
    }

    @Test
    public void testGetMethodWithBodyFail() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);
        proxy.testGetMethodWithBody(new DummyAccountInfo());
        // No assertions here, but a warning (or two) about a GET request with a body should be logged.
    }

    @Test
    public void testInterceptor() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), null, 500);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler, new HttpCodeExceptionInterceptor());

        assertThat(catchThrowable(proxy::test500))
                .isInstanceOf(Http500Exception.class);
    }

    @Test
    public void testEquals() throws Exception {
        ExampleService service = RestProxyFactory.createProxy(ExampleService.class, "http://example.com");
        assertThat(service).isNotEqualTo("something");
    }

    @Test
    public void testHashCode() throws Exception {
        ExampleService service = RestProxyFactory.createProxy(ExampleService.class, "http://example.com");
        new HashMap<ExampleService, Object>().put(service, 1);
    }

    @Test
    public void testInvocationAwareException() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{}", 500);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        ExampleInvocationAwareException ex = ExceptionUtils.catchException(ExampleInvocationAwareException.class, proxy::invocationAwareException);
        assertThat(ex).isInstanceOf(ExampleInvocationAwareException.class);
        assertThat(ex.getInvocation()).isNotNull();
        //noinspection ConstantConditions
        assertThat(ex.getInvocation().getHttpMethod()).isEqualTo("GET");
    }

    @Test
    public void responseHeadersAwareException() throws Exception {
        final Map<String, List<String>> mockHeaders = new HashMap<>();
        mockHeaders.put("X-my-header", Collections.singletonList("My value"));
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{}", 500) {
            @Override
            protected HttpURLConnection invokeHttp(RestInvocation invocation) {
                super.invokeHttp(invocation);
                HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
                Mockito.when(mockConnection.getHeaderFields()).thenReturn(mockHeaders);
                return mockConnection;
            }
        };
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        ExampleResponseHeadersAwareException e = ExceptionUtils.catchException(ExampleResponseHeadersAwareException.class, proxy::responseHeadersAwareException);
        assertThat(e).isInstanceOf(ExampleResponseHeadersAwareException.class);
        Map<String, List<String>> actualHeaders = e.getResponseHeaders();
        assertThat(actualHeaders).isNotNull()
                .containsEntry("X-my-header", Collections.singletonList("My value"));
    }

    @Test
    public void responseHeadersAwareException500() throws Exception {
        final Map<String, List<String>> mockHeaders = new HashMap<>();
        mockHeaders.put("X-my-header", Collections.singletonList("My value"));
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{}", 500) {
            @Override
            protected HttpURLConnection invokeHttp(RestInvocation invocation) {
                super.invokeHttp(invocation);
                HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
                Mockito.when(mockConnection.getHeaderFields()).thenReturn(mockHeaders);
                return mockConnection;
            }
        };
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        HttpStatusIOException e = ExceptionUtils.catchException(HttpStatusIOException.class, proxy::test500);
        assertThat(e).isInstanceOf(HttpResponseAware.class);
        Map<String, List<String>> actualHeaders = e.getResponseHeaders();
        assertThat(actualHeaders).isNotNull()
                .containsEntry("X-my-header", Collections.singletonList("My value"));
    }

    @Test
    public void responseHeadersAwareResult() throws Exception {
        final Map<String, List<String>> mockHeaders = new HashMap<>();
        mockHeaders.put("X-my-header-1", Collections.singletonList("My value for result"));
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{}", 200) {
            @Override
            protected HttpURLConnection invokeHttp(RestInvocation invocation) {
                super.invokeHttp(invocation);
                HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
                Mockito.when(mockConnection.getHeaderFields()).thenReturn(mockHeaders);
                return mockConnection;
            }
        };
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        final ExampleResponseHeadersAwareResult result = proxy.getResultWithResponseHeaders();

        Map<String, List<String>> actualHeaders = result.getResponseHeaders();
        assertThat(actualHeaders)
                .isNotNull()
                .containsEntry("X-my-header-1", Collections.singletonList("My value for result"));
    }

    @Test
    public void shouldUrlEncodePathParams() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{}", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.removeEntity("Entity name");

        assertThat(testHandler.getInvocation().getInvocationUrl()).contains("Entity+name");
    }

    @Test
    public void shouldDigestUrlEncodedFormParams() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), "{}", 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        final MockParamsDigest digest = new MockParamsDigest();
        proxy.getFuturesOrders("1233455,1234324,2123131", digest);

        assertThat(digest.requestBody).isEqualTo("order_id=1233455%2C1234324%2C2123131");
    }

    @DataProvider(parallel = true)
    public Object[][] randomSeed() {
        int rounds = 200;
        Object[][] result = new Object[rounds][];
        for (int i = 0; i < rounds; i++) {
            // try to distribute seed values across the long domain
            result[i] = new Object[]{(1L<<i)+i};
        }
        return result;
    }

    /** NOTE: this test sometimes fails. Not sure how to fix it. */
    @Test(dataProvider = "randomSeed")
    public void shouldReceiveSequentialNonces(long randomSeed) throws Exception {
        final InvocationHandler testHandler = new NonceCheckingDelayingInvocationHandler(randomSeed);

        final ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        final SynchronizedValueFactory<Long> vf = new LongValueFactory();

        final ExecutorService threadPool = Executors.newFixedThreadPool(5);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Future<Boolean> test = threadPool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    log.trace("Submitting call");
                    proxy.getNonce(vf);
                    return true;
                }
            });
            futures.add(test);
        }
        for (Future<Boolean> future : futures) {
            assertThat(future.get(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    private static class MockParamsDigest implements ParamsDigest {

        private String requestBody;

        @Override public String digestParams(RestInvocation restInvocation) {
            requestBody = restInvocation.getRequestBody();
            return "";
        }
    }

    private static class NonceCheckingDelayingInvocationHandler extends RestInvocationHandler {
        private final Random random;
        private long maxNonce;

        NonceCheckingDelayingInvocationHandler(long randomSeed) {
            super(ExampleService.class, null, null);
            maxNonce = -1;
            random = new Random(randomSeed);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Long nonce;
            synchronized (this) {
                RestInvocation invocation = createInvocation(method, args);
                nonce = (Long) invocation.getParamValue(FormParam.class, "nonce");
                log.trace("Got nonce {}, maxNonce = {}", nonce, maxNonce);
                if (nonce <= maxNonce) {
                    throw new IllegalArgumentException("" + nonce);
                }
                maxNonce = nonce;
            }

            // Pause to simulate network lag
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

    }
}
