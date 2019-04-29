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

import javax.ws.rs.*;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Matija Mazi
 *
 * This is the metadata about a rest-enabled method. The metadata is read by reflection from the interface.
 */
public class RestMethodMetadata implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(RestMethodMetadata.class);

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> HTTP_METHOD_ANNS
            = Arrays.asList(GET.class, POST.class, PUT.class, OPTIONS.class, HEAD.class, DELETE.class, PATCH.class);

    private final Type returnType;
    private final HttpMethod httpMethod;
    private final String baseUrl;
    private final String intfacePath;
    private final String methodPathTemplate;
    private final Class<? extends Exception> exceptionType;
    private final String reqContentType;
    private final String resContentType;
    private final String methodName;
    private final Map<Class<? extends Annotation>,Annotation> methodAnnotationMap;
    private final Annotation[][] parameterAnnotations;

    public RestMethodMetadata(Type returnType, HttpMethod httpMethod,
                              String baseUrl, String intfacePath, String methodPathTemplate,
                              Class<? extends Exception> exceptionType, String reqContentType,
                              String resContentType, String methodName,
                              Map<Class<? extends Annotation>, Annotation> methodAnnotationMap,
                              Annotation[][] parameterAnnotations) {
        this.returnType = returnType;
        this.httpMethod = httpMethod;
        this.baseUrl = baseUrl;
        this.intfacePath = intfacePath;
        this.reqContentType = reqContentType;
        this.resContentType = resContentType;
        this.methodName = methodName;
        this.methodAnnotationMap = methodAnnotationMap;
        this.parameterAnnotations = parameterAnnotations;
        this.methodPathTemplate = methodPathTemplate == null ? "" : methodPathTemplate;
        this.exceptionType = exceptionType;
    }

    public static RestMethodMetadata create(Method method, String baseUrl, String intfacePath) {
        String methodName = method.getName();
        Map<Class<? extends Annotation>, Annotation> methodAnnotationMap
                = AnnotationUtils.getMethodAnnotationMap(method,
                        RestInvocation.PARAM_ANNOTATION_CLASSES);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Consumes consumes = AnnotationUtils.getFromMethodOrClass(method, Consumes.class);
        String reqContentType = consumes != null ? consumes.value()[0] : null;
        Produces produces = AnnotationUtils.getFromMethodOrClass(method, Produces.class);
        String resContentType = produces != null ? produces.value()[0] : null;
        Path pathAnn = method.getAnnotation(Path.class);
        String methodPathTemplate = pathAnn == null ? "" : pathAnn.value();
        HttpMethod httpMethod = getHttpMethod(method);
        Class<?>[] thrownExceptions = method.getExceptionTypes();
        Class<? extends Exception> exceptionType = null;
        for (Class thrownException : thrownExceptions) {
            if (!IOException.class.isAssignableFrom(thrownException)) {
                if (!Exception.class.isAssignableFrom(thrownException)) {
                    throw new IllegalArgumentException("The only Throwables allowed on API methods are Exceptions; this method doesn't comply: " + method);
                }
                if (exceptionType != null) {
                    throw new IllegalArgumentException("Apart from IOException, at most one Exception is supported on an API method; this method has more: " + method);
                }
                //noinspection unchecked
                exceptionType = (Class<? extends Exception>) thrownException;
            }
        }

        // Do some validation.
        if (consumes != null && Arrays.asList(HttpMethod.DELETE, HttpMethod.GET).contains(httpMethod)) {
            log.warn("{} request declared as consuming method body as {}. While body is allowed, it should be ignored by the server. Is this intended? Method: {}", httpMethod, reqContentType, method);
        }

        return new RestMethodMetadata(method.getGenericReturnType(), httpMethod,
                baseUrl, intfacePath, methodPathTemplate, exceptionType,
                reqContentType, resContentType, methodName, methodAnnotationMap, parameterAnnotations);
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

    /**
     * @return the returnType
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * @return the httpMethod
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return the intfacePath
     */
    public String getIntfacePath() {
        return intfacePath;
    }

    /**
     * @return the methodPathTemplate
     */
    public String getMethodPathTemplate() {
        return methodPathTemplate;
    }

    /**
     * @return the exceptionType
     */
    public Class<? extends Exception> getExceptionType() {
        return exceptionType;
    }

    /**
     * @return the contentType
     */
    public String getReqContentType() {
        return reqContentType;
    }

    public String getResContentType() {
        return resContentType;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the methodAnnotationMap
     */
    public Map<Class<? extends Annotation>,Annotation> getMethodAnnotationMap() {
        return methodAnnotationMap;
    }

    /**
     * @return the parameterAnnotations
     */
    public Annotation[][] getParameterAnnotations() {
        return parameterAnnotations;
    }
}
