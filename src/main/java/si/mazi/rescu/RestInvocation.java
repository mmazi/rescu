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

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This holds name-value mapping for various types of params used in REST (QueryParam, PathParam, FormParam, HeaderParam).
 *
 * One RestInvocation instance corresponds to one method invocation.
 *
 * todo: A better name for this class would be RestInvocation.
 *
 * @author Matija Mazi
 */
public class RestInvocation implements Serializable {

    @SuppressWarnings("unchecked")
    protected static final List<Class<? extends Annotation>> PARAM_ANNOTATION_CLASSES = Arrays.asList(QueryParam.class, PathParam.class, FormParam.class, HeaderParam.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Class<? extends Annotation>, Params> paramsMap;
    private final List<Object> unannanotatedParams = new ArrayList<Object>();

    private String contentType;
    private String methodPath;
    private String invocationUrl;
    private String queryString;
    private String path;

    private RestMethodMetadata restMethodMetadata;

    RestInvocation(RestMethodMetadata restMethodMetadata, Object[] args) {
        this.restMethodMetadata = restMethodMetadata;

        paramsMap = new HashMap<Class<? extends Annotation>, Params>();
        for (Class<? extends Annotation> annotationClass : PARAM_ANNOTATION_CLASSES) {
            paramsMap.put(annotationClass, Params.of());
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
        path = restMethodMetadata.intfacePath;
        path = appendIfNotEmpty(path, methodPath, "/");
        queryString = paramsMap.get(QueryParam.class).asQueryString();

        invocationUrl = getInvocationUrl(restMethodMetadata.baseUrl, path, queryString);

        for (int i = 0; i < unannanotatedParams.size(); i++) {
            Object param = unannanotatedParams.get(i);
            if (param instanceof ParamsDigest) {
                unannanotatedParams.set(i, ((ParamsDigest) param).digestParams(this));
            }
        }
        for (Params params : paramsMap.values()) {
            params.digestAll(this);
        }
    }

    // todo: this is needed only for testing
    public RestInvocation(Map<Class<? extends Annotation>, Params> paramsMap, String contentType) {

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
        if (!url.isEmpty() && next != null && !next.isEmpty()) {
            if (!url.endsWith(separator) && !next.startsWith(separator)) {
                url += separator;
            }
            url += next;
        }
        return url;
    }

    public String getPath(String methodPath) {

        return paramsMap.get(PathParam.class).applyToPath(methodPath);
    }

    public String getRequestBody() {

        if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
            return paramsMap.get(FormParam.class).asFormEncodedRequestBody();
        } else if (MediaType.APPLICATION_JSON.equals(contentType)) {
            if (!paramsMap.get(FormParam.class).isEmpty()) {
                throw new IllegalArgumentException("@FormParams are not allowed with " + MediaType.APPLICATION_JSON);
            } else if (unannanotatedParams.size() > 1) {
                throw new IllegalArgumentException("Can only have a single unnanotated parameter with " + MediaType.APPLICATION_JSON);
            }
            if (unannanotatedParams.size() == 0) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(unannanotatedParams.get(0));
            } catch (IOException e) {
                throw new RuntimeException("Error writing json, probably a bug.", e);
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
     * @return The part of the invocation url that follows the '?' charater, ie. the &-separated name=value parameter pairs.
     */
    public String getQueryString() {
        return queryString;
    }

    public RestMethodMetadata getRestMethodMetadata() {
        return restMethodMetadata;
    }
}
