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

import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matija Mazi
 */
public class RestInvocationHandler implements InvocationHandler {

    private final HttpTemplate httpTemplate;
    private final String intfacePath;
    private final String baseUrl;
    private final ClientConfig config;

    private final Map<Method, RestMethodMetadata> cache = new HashMap<Method, RestMethodMetadata>();

    public RestInvocationHandler(Class<?> restInterface, String url, ClientConfig config) {
        this.config = config;
        this.intfacePath = restInterface.getAnnotation(Path.class).value();
        this.baseUrl = url;
        this.httpTemplate = new HttpTemplate();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RestMethodMetadata restMethodMetadata = getMetadata(method);
        RestInvocation params = new RestInvocation(restMethodMetadata, args, config == null ? null : config.getParamsMap());
        return invokeHttp(params);
    }

    private RestMethodMetadata getMetadata(Method method) {
        RestMethodMetadata metadata = cache.get(method);
        if (metadata == null) {
            metadata = RestMethodMetadata.create(method, baseUrl, intfacePath);
            cache.put(method, metadata);
        }
        return metadata;
    }

    protected Object invokeHttp(RestInvocation invocation) throws IOException {
        RestMethodMetadata methodMetadata = invocation.getRestMethodMetadata();
        final String requestBody = invocation.getContentType() == null ? null : invocation.getRequestBody();
        return httpTemplate.executeRequest(invocation.getInvocationUrl(), methodMetadata.returnType,
                requestBody, invocation.getHttpHeaders(), methodMetadata.httpMethod, invocation.getContentType(),
                methodMetadata.exceptionType);
    }

}
