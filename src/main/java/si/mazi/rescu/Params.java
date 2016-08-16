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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This class provides support for various types of HTTP params, especially in the context of RESTful web services, but may be also used to construct urls in other contexts.
 * </p>
 * <p>
 * Eg. this can be used to produce a URL query string:
 * </p>
 * <p>
 * Params.of("username", "john", "score", 2, "answer", "yes/no").asQueryString()
 * </p>
 * <p>
 * will produce:
 * </p>
 * <p>
 * username=john&amp;score=2&amp;answer=yes%2Fno
 * </p>
 *
 * @author Matija Mazi
 */
public final class Params implements Serializable {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private final Map<String, Object> data = new LinkedHashMap<>();
    private final DateFormat iso8601datetime;
    private final DateFormat iso8601date;

    {
        iso8601datetime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        iso8601datetime.setTimeZone(UTC);
        iso8601date = new SimpleDateFormat("yyyy-MM-dd");
        iso8601date.setTimeZone(UTC);
    }

    /**
     * private Constructor to prevent instantiation
     */
    private Params() {
    }

    public static Params of() {
        return new Params();
    }

    public static Params of(String param, Object value) {
        return of().add(param, value);
    }

    public static Params of(String p1, Object v1, String p2, Object v2) {
        return of(p1, v1).add(p2, v2);
    }

    public static Params of(String p1, Object v1, String p2, Object v2, String p3, Object v3) {
        return of(p1, v1, p2, v2).add(p3, v3);
    }

    public static Params of(String p1, Object v1, String p2, Object v2, String p3, Object v3, String p4, Object v4) {
        return of(p1, v1, p2, v2, p3, v3).add(p4, v4);
    }

    public Params add(String param, Object value) {
        data.put(param, value);
        return this;
    }

    private String toQueryString(boolean encode) {
        StringBuilder b = new StringBuilder();
        for (String paramName : data.keySet()) {
            if (isParamSet(paramName)) {
                Object originalValue = getParamValue(paramName);
                boolean createArrayParameters = originalValue instanceof Iterable && paramName.endsWith("[]");
                @SuppressWarnings("unchecked")
                Iterable<Object> paramValues = createArrayParameters
                        ? (Iterable<Object>)originalValue
                        : Collections.singleton(originalValue);
                for (Object paramValue : paramValues) {
                    if (b.length() > 0) {
                        b.append('&');
                    }
                    String paramValueAsString = toString(paramValue);
                    b.append(paramName).append('=').append(urlEncode(paramValueAsString, encode));
                }
            }
        }
        return b.toString();
    }

    private String urlEncode(String data, boolean encode) {
        try {
            return encode ? URLEncoder.encode(data, "UTF-8") : data;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Illegal encoding, fix the code.", e); // should not happen
        }
    }

    public String asQueryString() {
        return toQueryString(true);
    }

    public String asFormEncodedRequestBody() {
        return toQueryString(true);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public String applyToPath(String path) {
        for (String paramName : data.keySet()) {
            if (!isParamSet(paramName)) {
                throw new IllegalArgumentException("The value of '" + paramName + "' path parameter was not specified.");
            }
            path = Pattern.compile("\\{" + paramName + "(:.+?)?\\}").matcher(
                path).replaceAll(Matcher.quoteReplacement(urlEncode(getParamValueAsString(paramName), true)));
        }
        return path;
    }

    public Map<String, String> asHttpHeaders() {
        Map<String, String> stringMap = new LinkedHashMap<>();
        for (String key : data.keySet()) {
            if (isParamSet(key)) {
                stringMap.put(key, getParamValueAsString(key));
            }
        }
        return stringMap;
    }

    private String getParamValueAsString(String key) {
        Object paramValue = getParamValue(key);
        return toString(paramValue);
    }

    String toString(Object paramValue) {
        if (paramValue instanceof BigDecimal) {
            return ((BigDecimal) paramValue).toPlainString();
        } else if (paramValue instanceof Iterable) {
            return iterableToString((Iterable) paramValue);
        } else if (paramValue instanceof java.sql.Date) {
            synchronized (iso8601date) {
                return iso8601date.format(paramValue);
            }
        } else if (paramValue instanceof Date) {
            synchronized (iso8601datetime) {
                return iso8601datetime.format(paramValue);
            }
        }
        return String.valueOf(paramValue);
    }

    String iterableToString(Iterable iterable) {
        final StringBuilder sb = new StringBuilder();
        for (Object o : iterable) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(toString(o));
        }
        return sb.toString();
    }

    public void digestAll(RestInvocation invocationParams) {
        for (String paramName : data.keySet()) {
            Object paramValue = getParamValue(paramName);
            if (paramValue instanceof ParamsDigest) {
                data.put(paramName, ((ParamsDigest) paramValue).digestParams(invocationParams));
            }
        }
    }

    public boolean isParamSet(String paramName) {
        return data.containsKey(paramName) && getParamValue(paramName) != null;
    }

    public Object getParamValue(String paramName) {
        return data.get(paramName);
    }

    public void replaceValueFactories(){
        for (Map.Entry<String, Object> e : data.entrySet()) {
            Object value = e.getValue();
            if(value instanceof SynchronizedValueFactory)
                e.setValue(((SynchronizedValueFactory) value).createValue());
        }
    }

    @Override
    public String toString() {
        return toQueryString(false);
    }

}
