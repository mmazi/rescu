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

import javax.ws.rs.*;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matija Mazi
 *
 * This is the metadata about a rest-enabled method. The metadata is read by reflection from the interface.
 */
class RestMethodMetadata implements Serializable {

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> HTTP_METHOD_ANNS = Arrays.asList(GET.class, POST.class, PUT.class, OPTIONS.class, HEAD.class, DELETE.class);

    protected final Class<?> returnType;
    protected final HttpMethod httpMethod;
    protected final String baseUrl;
    protected final String intfacePath;
    protected final String methodPathTemplate;

    private RestMethodMetadata(Class<?> returnType, HttpMethod httpMethod, String baseUrl, String intfacePath, String methodPathTemplate) {

        this.returnType = returnType;
        this.httpMethod = httpMethod;
        this.baseUrl = baseUrl;
        this.intfacePath = intfacePath;
        this.methodPathTemplate = methodPathTemplate;
    }

    static RestMethodMetadata create(Method method, String baseUrl, String intfacePath) {

        Path pathAnn = method.getAnnotation(Path.class);
        String methodPathTemplate = pathAnn == null ? null : pathAnn.value();
        HttpMethod httpMethod = getHttpMethod(method);
        return new RestMethodMetadata(method.getReturnType(), httpMethod, baseUrl, intfacePath, methodPathTemplate);
    }

    static HttpMethod getHttpMethod(Method method) {

        HttpMethod httpMethod = null;
        for (Class<? extends Annotation> m : HTTP_METHOD_ANNS) {
            if (method.isAnnotationPresent(m)) {
                if (httpMethod != null) {
                    throw new IllegalArgumentException("Method is annotated with more than one HTTP-method annotation: " + method);
                }
                httpMethod = HttpMethod.valueOf(m.getSimpleName());
            }
        }
        if (httpMethod == null) {
            throw new IllegalArgumentException("Method must be annotated with a HTTP-method annotation: " + method);
        }
        return httpMethod;
    }

    public String getInvocationUrl(RestInvocationParams params) {
        String methodPath = methodPathTemplate == null ? null : params.getPath(methodPathTemplate);
        return getInvocationUrl(baseUrl, intfacePath, methodPath, params.getQueryString());
    }

    private String getInvocationUrl(String baseUrl, String intfacePath, String method, String queryString) {

        // TODO make more robust in terms of path separator ('/') handling
        // (Use UriBuilder?)
        String completeUrl = baseUrl;
        completeUrl = appendIfNotEmpty(completeUrl, intfacePath, "/");
        completeUrl = appendIfNotEmpty(completeUrl, method, "/");
        completeUrl = appendIfNotEmpty(completeUrl, queryString, "?");
        return completeUrl;
    }

    private static String appendIfNotEmpty(String url, String next, String separator) {

        if (next != null && next.trim().length() > 0 && !next.equals("/")) {
            url += separator + next;
        }
        return url;
    }

}
