/*
 * The MIT License
 *
 * Copyright 2014 RedDragCZ.
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

import org.testng.annotations.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

/**
 *
 * @author RedDragCZ
 */
public class RestInvocationTest {
    
    public RestInvocationTest() {
    }
    /**
     * Test of create method, of class RestInvocation.
     */
    @Test
    public void testCreateWithParamsDigest() {
        Map<Class<? extends Annotation>, Params> paramsMap = new HashMap<Class<? extends Annotation>, Params>();
        paramsMap.put(FormParam.class, Params.of("nonce", 1328626350245256L));
        paramsMap.put(HeaderParam.class, Params.of("digest", HmacPostBodyDigest.createInstance("9WkB3zUil6h5pXrqUX7XT57c+g2rxxemeGYv3aBSW4hlkwSIgmul+mC3yxwU8fPtQsR8jTpyI2xo7WznjhTf4g==")));

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
    }

    Long nonce = 1328626350245256L;

    @Test
    public void testCreateWithValueGenerator() {
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
        assertThat(requestBody, containsString("data=first,second"));
    }

    @Test
    public void testFormPostCollectionArray() throws Exception {
        ClientConfig config = new ClientConfig();

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class, config, null, 200);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testFromPostCollectionAsArray(Arrays.asList("first", "second"));

        final String requestBody = testHandler.getInvocation().getRequestBody();
        assertThat(requestBody, containsString("data[]=first"));
        assertThat(requestBody, containsString("data[]=second"));
    }
}
