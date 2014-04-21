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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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

    private final ObjectMapper objectMapper;
    private final Map<Class<? extends Annotation>, Params> paramsMap;
    private final List<Object> unannanotatedParams = new ArrayList<Object>();

    private String contentType;
    private String methodPath;
    private String invocationUrl;
    private String queryString;
    private String path;

    private RestMethodMetadata restMethodMetadata;

    RestInvocation(ObjectMapper objectMapper, RestMethodMetadata restMethodMetadata, Object[] args, Map<Class<? extends Annotation>, Params> defaultParamsMap) {
        this.objectMapper = objectMapper;
        this.restMethodMetadata = restMethodMetadata;

        paramsMap = new HashMap<Class<? extends Annotation>, Params>();
        for (Class<? extends Annotation> annotationClass : PARAM_ANNOTATION_CLASSES) {
            this.paramsMap.put(annotationClass, Params.of());
        }
        if (defaultParamsMap != null) {
            paramsMap.putAll(defaultParamsMap);
        }

        Annotation[][] paramAnnotations = restMethodMetadata.parameterAnnotations;
        for (int i = 0; i < paramAnnotations.length; i++) {
            Annotation[] paramAnns = paramAnnotations[i];
            if (paramAnns.length == 0) {
                unannanotatedParams.add(args[i]);
            }
            for (Annotation paramAnn : paramAnns) {
                String paramName = getParamName(paramAnn);
                if (paramName != null) {
                    this.paramsMap.get(paramAnn.annotationType()).add(paramName, args[i]);
                }
            }
        }

        Map<Class<? extends Annotation>, Annotation> methodAnnotationMap = restMethodMetadata.methodAnnotationMap;

        // Support using method name as a parameter.
        for (Class<? extends Annotation> paramAnnotationClass : methodAnnotationMap.keySet()) {
            Annotation annotation = methodAnnotationMap.get(paramAnnotationClass);
            if (annotation != null) {
                String paramName = getParamName(annotation);
                this.paramsMap.get(paramAnnotationClass).add(paramName, restMethodMetadata.methodName);
            }
        }

        contentType = restMethodMetadata.contentType;
        methodPath = getPath(restMethodMetadata.methodPathTemplate);
        
        path = getPath(restMethodMetadata.intfacePath);
        path = appendIfNotEmpty(path, methodPath, "/");
        
        queryString = this.paramsMap.get(QueryParam.class).asQueryString();

        invocationUrl = getInvocationUrl(restMethodMetadata.baseUrl, path, queryString);

        for (int i = 0; i < unannanotatedParams.size(); i++) {
            Object param = unannanotatedParams.get(i);
            if (param instanceof ParamsDigest) {
                unannanotatedParams.set(i, ((ParamsDigest) param).digestParams(this));
            }
        }
        for (Params params : this.paramsMap.values()) {
            params.digestAll(this);
        }
    }

    // todo: this is needed only for testing
    public RestInvocation(ObjectMapper objectMapper, Map<Class<? extends Annotation>, Params> paramsMap, String contentType) {
        this.objectMapper = objectMapper;
        this.contentType = contentType;
        this.paramsMap = new LinkedHashMap<Class<? extends Annotation>, Params>(paramsMap);
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

    public final String getPath(String methodPath) {
        return paramsMap.get(PathParam.class).applyToPath(methodPath);
    }

    public String getRequestBody() {

        if (contentType == null) {
            throw new IllegalArgumentException("No media type specified; don't know how to create request body. Please specify the body media type using @javax.ws.rs.Consumes.");
        }

        if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
            return paramsMap.get(FormParam.class).asFormEncodedRequestBody();
        } else if (MediaType.APPLICATION_JSON.equals(contentType)) {
            if (!paramsMap.get(FormParam.class).isEmpty()) {
                throw new IllegalArgumentException("@FormParams are not allowed with " + MediaType.APPLICATION_JSON);
            } else if (unannanotatedParams.size() > 1) {
                throw new IllegalArgumentException("Can only have a single unannotated parameter with " + MediaType.APPLICATION_JSON);
            }
            if (unannanotatedParams.size() == 0) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(unannanotatedParams.get(0));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error writing json, probably a bug in rescu.", e);
            }
        }
        throw new IllegalArgumentException("Unsupported media type: " + contentType);
    }

    public Map<String, String> getHttpHeaders() {

        return paramsMap.get(HeaderParam.class).asHttpHeaders();
    }

    public String getContentType() {
        return contentType;
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
        return restMethodMetadata.baseUrl;
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
        return restMethodMetadata.httpMethod.toString();
    }

    public RestMethodMetadata getRestMethodMetadata() {
        return restMethodMetadata;
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
        return paramsMap.get(paramAnnotation).getParamValue(paramName);
    }
}
