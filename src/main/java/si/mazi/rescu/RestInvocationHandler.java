/**
 * Copyright (C) 2013 Matija Mazi
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.serialization.PlainTextResponseReader;
import si.mazi.rescu.serialization.ToStringRequestWriter;
import si.mazi.rescu.serialization.jackson.JacksonMapper;
import si.mazi.rescu.serialization.jackson.JacksonRequestWriter;
import si.mazi.rescu.serialization.jackson.JacksonResponseReader;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matija Mazi
 */
public class RestInvocationHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(RestInvocationHandler.class);

    private final ResponseReaderResolver responseReaderResolver;
    private final RequestWriterResolver requestWriterResolver;

    private final HttpTemplate httpTemplate;
    private final String intfacePath;
    private final String baseUrl;
    private final ClientConfig config;

    private final Map<Method, RestMethodMetadata> methodMetadataCache = new HashMap<>();

    RestInvocationHandler(Class<?> restInterface, String url, ClientConfig config) {
        this.intfacePath = restInterface.getAnnotation(Path.class).value();
        this.baseUrl = url;

        if (config == null) {
            config = new ClientConfig(); //default config
        }

        this.config = config;

        //setup default readers/writers
        JacksonMapper jacksonMapper = new JacksonMapper(config.getJacksonObjectMapperFactory());

        requestWriterResolver = new RequestWriterResolver();
        /*requestWriterResolver.addWriter(null,
                new NullRequestWriter());*/
        requestWriterResolver.addWriter(MediaType.APPLICATION_FORM_URLENCODED,
                new FormUrlEncodedRequestWriter());
        requestWriterResolver.addWriter(MediaType.APPLICATION_JSON,
                new JacksonRequestWriter(jacksonMapper));
        requestWriterResolver.addWriter(MediaType.TEXT_PLAIN,
                new ToStringRequestWriter());

        responseReaderResolver = new ResponseReaderResolver();
        responseReaderResolver.addReader(MediaType.APPLICATION_JSON,
                new JacksonResponseReader(jacksonMapper, this.config.isIgnoreHttpErrorCodes()));
        responseReaderResolver.addReader(MediaType.TEXT_PLAIN,
                new PlainTextResponseReader(this.config.isIgnoreHttpErrorCodes()));

                //setup http client
        this.httpTemplate = new HttpTemplate(
                this.config.getHttpConnTimeout(),
                this.config.getHttpReadTimeout(),
                this.config.getProxyHost(), this.config.getProxyPort(),
                this.config.getSslSocketFactory(), this.config.getHostnameVerifier(), this.config.getOAuthConsumer());
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        RestMethodMetadata methodMetadata = getMetadata(method);

        HttpURLConnection connection = null;
        RestInvocation invocation = null;
        Object lock = getValueGenerator(args);
        if (lock == null) {
            lock = new Object(); // effectively no locking
        }
        try {
            synchronized (lock) {
                invocation = RestInvocation.create(
                        requestWriterResolver, methodMetadata, args, config.getDefaultParamsMap());
                connection = invokeHttp(invocation);
            }
            return receiveAndMap(methodMetadata, connection);
        } catch (Exception e) {
            boolean shouldWrap = config.isWrapUnexpectedExceptions();
            if (e instanceof InvocationAware) {
                try {
                    ((InvocationAware) e).setInvocation(invocation);
                    shouldWrap = false;
                } catch (Exception ex) {
                    log.warn("Failed to set invocation on the InvocationAware", ex);
                }
            }
            if (e instanceof HttpResponseAware && connection != null) {
                try {
                    ((HttpResponseAware) e).setResponseHeaders(connection.getHeaderFields());
                    shouldWrap = false;
                } catch (Exception ex) {
                    log.warn("Failed to set response headers on the HttpReponseAware", ex);
                }
            }
            if (shouldWrap) {
                throw new AwareException(e, invocation);
            }
            throw e;
        }
    }

    protected HttpURLConnection invokeHttp(RestInvocation invocation) throws IOException {
        RestMethodMetadata methodMetadata = invocation.getMethodMetadata();

        RequestWriter requestWriter = requestWriterResolver.resolveWriter(invocation.getMethodMetadata());
        final String requestBody = requestWriter.writeBody(invocation);

        return httpTemplate.send(invocation.getInvocationUrl(), requestBody, invocation.getAllHttpHeaders(), methodMetadata.getHttpMethod());
    }

    protected Object receiveAndMap(RestMethodMetadata methodMetadata, HttpURLConnection connection) throws IOException {
        InvocationResult invocationResult = httpTemplate.receive(connection);
        return mapInvocationResult(invocationResult, methodMetadata);
    }

    private static SynchronizedValueFactory getValueGenerator(Object[] args) {
        if (args != null) for (Object arg : args)
            if (arg instanceof SynchronizedValueFactory)
                return (SynchronizedValueFactory) arg;
        return null;
    }

    protected Object mapInvocationResult(InvocationResult invocationResult,
            RestMethodMetadata methodMetadata) throws IOException {
        return responseReaderResolver.resolveReader(methodMetadata).read(invocationResult, methodMetadata);
    }

    private RestMethodMetadata getMetadata(Method method) {
        RestMethodMetadata metadata = methodMetadataCache.get(method);
        if (metadata == null) {
            metadata = RestMethodMetadata.create(method, baseUrl, intfacePath);
            methodMetadataCache.put(method, metadata);
        }
        return metadata;
    }
}
