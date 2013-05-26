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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Matija Mazi
 */
public class RestInvocationHandler implements InvocationHandler {

    private final HttpTemplate httpTemplate;
    private final String intfacePath;
    private final String baseUrl;

    public RestInvocationHandler(Class<?> restInterface, String url) {

        this.intfacePath = restInterface.getAnnotation(Path.class).value();
        this.baseUrl = url;
        this.httpTemplate = new HttpTemplate();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RestMethodMetadata restMethodMetadata = RestMethodMetadata.create(method, baseUrl, intfacePath); // todo: this may be cached for method
        RestInvocationParams params = new RestInvocationParams(restMethodMetadata, args);
        return invokeHttp(restMethodMetadata, params);
    }

    protected Object invokeHttp(RestMethodMetadata restMethodMetadata, RestInvocationParams params) {

        return httpTemplate.executeRequest(params.getInvocationUrl(), restMethodMetadata.returnType,
                params.getRequestBody(), params.getHttpHeaders(), restMethodMetadata.httpMethod, params.getContentType(),
                restMethodMetadata.exceptionType);
    }

}
