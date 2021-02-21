/*
 * The MIT License
 *
 * Copyright 2014.
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package si.mazi.rescu;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class RestInvocationTest {

    private static final Logger log = LoggerFactory.getLogger(RestInvocationTest.class);

    @Test
    public void testCreateWithParamsDigest() throws UnsupportedEncodingException {
        Map<Class<? extends Annotation>, Params> paramsMap = new HashMap<Class<? extends Annotation>, Params>();
        paramsMap.put(FormParam.class, Params.of("nonce", 1328626350245256L));
        paramsMap.put(HeaderParam.class, Params.of("digest", HmacPostBodyDigest.createInstance("9WkB3zUil6h5pXrqUX7XT57c+g2rxxemeGYv3aBSW4hlkwSIgmul+mC3yxwU8fPtQsR8jTpyI2xo7WznjhTf4g==")));
        paramsMap.put(QueryParam.class, Params.of("digest", HmacPostBodyDigest.createInstance("9WkB3zUil6h5pXrqUX7XT57c+g2rxxemeGYv3aBSW4hlkwSIgmul+mC3yxwU8fPtQsR8jTpyI2xo7WznjhTf4g==")));

        RequestWriterResolver requestWriterResolver = new RequestWriterResolver();
        requestWriterResolver.addWriter(MediaType.APPLICATION_FORM_URLENCODED, new FormUrlEncodedRequestWriter());

        RestInvocation invocation = RestInvocation.create(requestWriterResolver,
                new RestMethodMetadata(String.class, HttpMethod.GET,
                        "http://example.com", "/api", null,
                        RuntimeException.class, MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, null,
                        new HashMap<Class<? extends Annotation>, Annotation>(),
                        new Annotation[][] {}),
                new Object[] {}, paramsMap);

        assertEquals("eNjLVoVh6LVQfzgv7qFMCL48b5d2Qd1gvratXGA76W6+g46Jl9TNkiTCHks5sLXjfAQ1rGnvWxRHu6pYjC5FSQ==",
                invocation.getParamValue(HeaderParam.class, "digest"));

        assertEquals("digest=" + URLEncoder.encode("eNjLVoVh6LVQfzgv7qFMCL48b5d2Qd1gvratXGA76W6+g46Jl9TNkiTCHks5sLXjfAQ1rGnvWxRHu6pYjC5FSQ==", "UTF-8"),
                invocation.getQueryString());

        assertThat(invocation.getInvocationUrl()).contains(URLEncoder.encode("eNjLVoVh6LVQfzgv7qFMCL48b5d2Qd1gvratXGA76W6+g46Jl9TNkiTCHks5sLXjfAQ1rGnvWxRHu6pYjC5FSQ==", "UTF-8"));
    }

    @Test
    public void testCreateWithValueGenerator() {
        Long nonce = 1328626350245256L;
        Map<Class<? extends Annotation>, Params> paramsMap = new HashMap<Class<? extends Annotation>, Params>();
        paramsMap.put(FormParam.class, Params.of("nonce", new ConstantValueFactory<Long>(nonce)));

        RequestWriterResolver requestWriterResolver = new RequestWriterResolver();
        requestWriterResolver.addWriter(MediaType.APPLICATION_FORM_URLENCODED, new FormUrlEncodedRequestWriter());

        RestInvocation invocation = RestInvocation.create(requestWriterResolver,
                new RestMethodMetadata(String.class, HttpMethod.GET,
                        "http://example.com", "/api", null,
                        RuntimeException.class, MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, null,
                        new HashMap<Class<? extends Annotation>, Annotation>(),
                        new Annotation[][] {}),
                new Object[] {}, paramsMap);

        assertEquals(invocation.getParamValue(FormParam.class, "nonce"), nonce);
    }

    @Test
    public void testFormPostCollectionDefault() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testFromPostCollection(Arrays.asList("first", "second"));

        final String requestBody = URLDecoder.decode(testHandler.getInvocation().getRequestBody(), "UTF-8");
        assertThat(requestBody).contains("data=first,second");
    }

    @Test
    public void testFormPostCollectionArray() throws Exception {
        ClientConfig config = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, config, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testFromPostCollectionAsArray(Arrays.asList("first", "second"));

        final String requestBody = testHandler.getInvocation().getRequestBody();
        assertThat(requestBody).contains("data[]=first");
        assertThat(requestBody).contains("data[]=second");
    }

    @Test
    public void testDateQueryParam() throws Exception {
        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, new ClientConfig(), null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        LocalDateTime dateTime = new LocalDateTime(2015, 5, 27, 14, 24, 11);
        TimeZone tz = TimeZone.getTimeZone("UTC");

        proxy.testDateQueryParam(dateTime.toDate(tz));

        String queryString = testHandler.getInvocation().getQueryString();
        int i = queryString.lastIndexOf('=');
        assertThat(i).isGreaterThan(0);
        String datetimeStr = URLDecoder.decode(queryString.substring(i + 1), "UTF-8");
        log.debug("datetimeStr = {}", datetimeStr);

        assertThat(datetimeStr).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(([+-][0-9]{2}:[0-9]{2})|Z)");
    }
}
