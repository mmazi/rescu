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


import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This holds name-value mapping for various types of params used in REST (QueryParam, PathParam, FormParam, HeaderParam).
 *
 * One RestInvocation instance corresponds to one method invocation.
 *
 * @author Matija Mazi
 */
public class RestInvocation implements Serializable {

    @SuppressWarnings("unchecked")
    protected static final List<Class<? extends Annotation>> PARAM_ANNOTATION_CLASSES = Arrays.asList(QueryParam.class, PathParam.class, FormParam.class, HeaderParam.class);

    private final Map<Class<? extends Annotation>, Params> paramsMap;
    private final List<Object> unannanotatedParams;
    private final RestMethodMetadata methodMetadata;
    private final String methodPath;
    private final String invocationUrl;
    private final String queryString;
    private final String path;
    private final RequestWriterResolver requestWriterResolver;

    public RestInvocation(Map<Class<? extends Annotation>, Params> paramsMap,
            List<Object> unannanotatedParams,
            RestMethodMetadata methodMetadata,
            String methodPath,
            String invocationUrl,
            String queryString,
            String path,
            RequestWriterResolver requestWriterResolver) {
        this.paramsMap = paramsMap;
        this.unannanotatedParams = unannanotatedParams;
        this.methodMetadata = methodMetadata;
        this.methodPath = methodPath;
        this.invocationUrl = invocationUrl;
        this.queryString = queryString;
        this.path = path;
        this.requestWriterResolver = requestWriterResolver;
    }

    static RestInvocation create(RequestWriterResolver requestWriterResolver,
            RestMethodMetadata methodMetadata,
            Object[] args,
            Map<Class<? extends Annotation>, Params> defaultParamsMap) {
        
        HashMap<Class<? extends Annotation>, Params> paramsMap = new HashMap<Class<? extends Annotation>, Params>();
        
        for (Class<? extends Annotation> annotationClass : PARAM_ANNOTATION_CLASSES) {
            paramsMap.put(annotationClass, Params.of());
        }
        
        if (defaultParamsMap != null) {
            paramsMap.putAll(defaultParamsMap);
        }

        List<Object> unannanotatedParams = new ArrayList<Object>();
        
        Annotation[][] paramAnnotations = methodMetadata.getParameterAnnotations();
        for (int i = 0; i < paramAnnotations.length; i++) {
            Annotation[] paramAnns = paramAnnotations[i];
            if (paramAnns.length == 0) {
                unannanotatedParams.add(args[i]);
            }
            for (Annotation paramAnn : paramAnns) {
                String paramName = getParamName(paramAnn);
                if (paramName != null) {
                    paramsMap.get(paramAnn.annotationType()).add(paramName, args[i]);
                }
            }
        }

        Map<Class<? extends Annotation>, Annotation> methodAnnotationMap = methodMetadata.getMethodAnnotationMap();

        // Support using method name as a parameter.
        for (Class<? extends Annotation> paramAnnotationClass : methodAnnotationMap.keySet()) {
            Annotation annotation = methodAnnotationMap.get(paramAnnotationClass);
            if (annotation != null) {
                String paramName = getParamName(annotation);
                paramsMap.get(paramAnnotationClass).add(paramName, methodMetadata.getMethodName());
            }
        }

        String methodPath = getPath(paramsMap, methodMetadata.getMethodPathTemplate());
        
        String path = getPath(paramsMap, methodMetadata.getIntfacePath());
        path = appendIfNotEmpty(path, methodPath, "/");
        
        String queryString = paramsMap.get(QueryParam.class).asQueryString();
        String invocationUrl = getInvocationUrl(methodMetadata.getBaseUrl(), path, queryString);

        RestInvocation invocation = new RestInvocation(
                paramsMap,
                unannanotatedParams,
                methodMetadata,
                methodPath,
                invocationUrl,
                queryString,
                path,
                requestWriterResolver);
        
        for (int i = 0; i < unannanotatedParams.size(); i++) {
            Object param = unannanotatedParams.get(i);
            if (param instanceof ParamsDigest) {
                unannanotatedParams.set(i, ((ParamsDigest) param).digestParams(invocation));
            }
        }
        
        for (Params params : paramsMap.values()) {
            params.digestAll(invocation);
        }
        
        return invocation;
    }

    private static String getParamName(Annotation queryParam) {

        for (Class<? extends Annotation> annotationClass : PARAM_ANNOTATION_CLASSES) {
            String paramName = AnnotationUtils.getValueOrNull(annotationClass, queryParam);
            if (paramName != null) {
                return paramName;
            }
        }
        // This is not one of the annotations in PARAM_ANNOTATION_CLASSES.
        return null;
    }

    static String getInvocationUrl(String baseUrl, String path, String queryString) {
        // TODO make more robust in terms of path separator ('/') handling
        // (Use UriBuilder?)
        String completeUrl = baseUrl;
        completeUrl = appendIfNotEmpty(completeUrl, path, "/");
        completeUrl = appendIfNotEmpty(completeUrl, queryString, "?");
        return completeUrl;
    }

    static String appendIfNotEmpty(String url, String next, String separator) {
        if (url.length() > 0 && next != null && next.length() > 0) {
            if (!url.endsWith(separator) && !next.startsWith(separator)) {
                url += separator;
            }
            url += next;
        }
        return url;
    }

    static String getPath(
            Map<Class<? extends Annotation>, Params> paramsMap, String methodPath) {
        return paramsMap.get(PathParam.class).applyToPath(methodPath);
    }

    public String getRequestBody() {
        return requestWriterResolver
                .resolveWriter(this)
                .writeBody(this);
    }
    
    public Map<String, String> getHttpHeaders() {
        return getParamsMap().get(HeaderParam.class).asHttpHeaders();
    }

    public String getContentType() {
        return methodMetadata.getContentType();
    }

    /**
     * @return The invocation url that is used in this invocation.
     */
    public String getInvocationUrl() {
        return invocationUrl;
    }

    /**
     * @return The part of the url path that corresponds to the method.
     */
    public String getMethodPath() {
        return methodPath;
    }

    /**
     * @return The whole url path: the interface path together with the method path.
     * This is usally the part of the url that follows the host name.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return The base of the url: this usually contains the protocol (eg. http) and the host name
     * (eg. http://www.example.com/) but may be longer.
     */
    public String getBaseUrl() {
        return getMethodMetadata().getBaseUrl();
    }

    /**
     * @return The part of the invocation url that follows the '?' charater, ie. the &amp;-separated name=value parameter pairs.
     */
    public String getQueryString() {
        return queryString;
    }
    
    /**
     * @return The HTTP method used in this invocation e.g. GET or POST
     */
    public String getHttpMethod() {
        return getMethodMetadata().getHttpMethod().toString();
    }

    public RestMethodMetadata getMethodMetadata() {
        return methodMetadata;
    }

    /**
     * @param paramAnnotation One of {@link javax.ws.rs.QueryParam}.class, {@link javax.ws.rs.PathParam}.class,
     *                         {@link javax.ws.rs.FormParam}.class, {@link javax.ws.rs.HeaderParam}.class
     * @param paramName       The name of the parameter, ie. the value of the value() element in the annotation.
     * @return                The actual value that was passed as the argument in the method call; null if either no
     *                        parameter with the given name exists or null was passed as argument.
     */
    public Object getParamValue(Class<? extends Annotation> paramAnnotation, String paramName) {
        if (!PARAM_ANNOTATION_CLASSES.contains(paramAnnotation)) {
            throw new IllegalArgumentException("Unsupported annotation type: " + paramAnnotation + ". Should be one of " + PARAM_ANNOTATION_CLASSES);
        }
        return getParamsMap().get(paramAnnotation).getParamValue(paramName);
    }

    /**
     * @return the paramsMap
     */
    public Map<Class<? extends Annotation>, Params> getParamsMap() {
        return paramsMap;
    }

    /**
     * @return the unannanotatedParams
     */
    public List<Object> getUnannanotatedParams() {
        return unannanotatedParams;
    }
}
