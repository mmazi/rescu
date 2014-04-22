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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import si.mazi.rescu.jackson.JacksonMapper;
import si.mazi.rescu.jackson.JacksonRequestWriter;
import si.mazi.rescu.jackson.JacksonResponseReader;

/**
 * @author Matija Mazi
 */
public class RestInvocationHandler implements InvocationHandler {

    private final ResponseReader responseReader;
    private final RequestWriterResolver requestWriterResolver;
    
    private final HttpTemplate httpTemplate;
    private final String intfacePath;
    private final String baseUrl;
    private final ClientConfig config;

    private final Map<Method, RestMethodMetadata> methodMetadataCache = new HashMap<Method, RestMethodMetadata>();

    public RestInvocationHandler(Class<?> restInterface, String url, ClientConfig config) {
        this.intfacePath = restInterface.getAnnotation(Path.class).value();
        this.baseUrl = url;
        
        if (config == null) {
            config = new ClientConfig(); //default config
        }
        
        this.config = config;
        
        //setup default readers/writers
        JacksonMapper jacksonMapper = new JacksonMapper(config.getJacksonConfigureListener());

        requestWriterResolver = new RequestWriterResolver();
        /*requestWriterResolver.addWriter(null,
                new NullRequestWriter());*/
        requestWriterResolver.addWriter(MediaType.APPLICATION_FORM_URLENCODED,
                new FormUrlEncodedRequestWriter());
        requestWriterResolver.addWriter(MediaType.APPLICATION_JSON,
                new JacksonRequestWriter(jacksonMapper));
        
        responseReader = new JacksonResponseReader(jacksonMapper,
            this.config.isIgnoreHttpErrorCodes());
        
        //setup http client
        this.httpTemplate = new HttpTemplate(
                this.config.getHttpReadTimeout(),
                this.config.getProxyHost(), this.config.getProxyPort(),
                this.config.getSslSocketFactory(), this.config.getHostnameVerifier());
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RestMethodMetadata methodMetadata = getMetadata(method);
        
        RestInvocation invocation = RestInvocation.create(
                requestWriterResolver,
                methodMetadata, args,
                config == null ? null : config.getParamsMap());
        
        InvocationResult invocationResult = invokeHttp(invocation);
        return mapInvocationResult(invocationResult, methodMetadata);
    }

    protected InvocationResult invokeHttp(RestInvocation invocation) throws IOException {
        RestMethodMetadata methodMetadata = invocation.getMethodMetadata();
        
        RequestWriter requestWriter = requestWriterResolver.resolveWriter(invocation);
        final String requestBody = requestWriter.writeBody(invocation);
        
        return httpTemplate.executeRequest(invocation.getInvocationUrl(),
                requestBody,
                invocation.getHttpHeaders(),
                methodMetadata.getHttpMethod(),
                invocation.getContentType());
    }
    
    protected Object mapInvocationResult(InvocationResult invocationResult,
            RestMethodMetadata methodMetadata) throws IOException {
        return responseReader.read(invocationResult, methodMetadata);
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
