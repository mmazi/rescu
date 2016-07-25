/*
 * Copyright (C) 2015 Matija Mazi
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
 *
 */
package si.mazi.rescu;

import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuthConsumer;
import si.mazi.rescu.serialization.jackson.DefaultJacksonObjectMapperFactory;
import si.mazi.rescu.serialization.jackson.JacksonConfigureListener;
import si.mazi.rescu.serialization.jackson.JacksonObjectMapperFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ClientConfig {

    private final Map<Class<? extends Annotation>, Params> defaultParamsMap = new HashMap<>();

    private SSLSocketFactory sslSocketFactory = null;
    private HostnameVerifier hostnameVerifier = null;
    private JacksonObjectMapperFactory jacksonObjectMapperFactory = null;
    private int httpConnTimeout;
    private int httpReadTimeout;
    private Integer proxyPort;
    private String proxyHost;
    private boolean ignoreHttpErrorCodes;
    private boolean wrapUnexpectedExceptions;
    private OAuthConsumer oAuthConsumer;

    public ClientConfig() {
        httpConnTimeout = Config.getHttpConnTimeout();
        httpReadTimeout = Config.getHttpReadTimeout();
        proxyPort = Config.getProxyPort();
        proxyHost = Config.getProxyHost();
        ignoreHttpErrorCodes = Config.isIgnoreHttpErrorCodes();
        wrapUnexpectedExceptions = Config.isWrapUnexpectedExceptions();
    }

    public ClientConfig addDefaultParam(Class<? extends Annotation> paramType, String paramName, Object paramValue) {
        Params params = defaultParamsMap.get(paramType);
        if (params == null) {
            params = Params.of();
            defaultParamsMap.put(paramType, params);
        }
        params.add(paramName, paramValue);
        return this;
    }

    public Map<Class<? extends Annotation>, Params> getDefaultParamsMap() {
        return defaultParamsMap;
    }

    /**
     * Gets the override SSL socket factory for HttpsURLConnection
     * used if HTTPS protocol is requested.
     *
     * @return the sslSocketFactory
     */
    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Sets the override SSL socket factory for HttpsURLConnection
     * used if HTTPS protocol is requested.
     *
     * @param sslSocketFactory the sslSocketFactory to set
     */
    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * Gets the override hostname verifier for HttpsURLConnection
     * used if HTTPS protocol is requested.
     *
     * @return the hostnameVerifier
     */
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Sets the override hostname for HttpsURLConnection
     * used if HTTPS protocol is requested.
     *
     * @param hostnameVerifier the hostnameVerifier to set
     */
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public int getHttpConnTimeout() {
        return httpConnTimeout;
    }

    public void setHttpConnTimeout(int httpConnTimeout) {
        this.httpConnTimeout = httpConnTimeout;
    }

    public int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public void setHttpReadTimeout(int httpReadTimeout) {
        this.httpReadTimeout = httpReadTimeout;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public boolean isIgnoreHttpErrorCodes() {
        return ignoreHttpErrorCodes;
    }

    public void setIgnoreHttpErrorCodes(boolean ignoreHttpErrorCodes) {
        this.ignoreHttpErrorCodes = ignoreHttpErrorCodes;
    }

    public boolean isWrapUnexpectedExceptions() {
        return wrapUnexpectedExceptions;
    }

    public void setWrapUnexpectedExceptions(boolean wrapUnexpectedExceptions) {
        this.wrapUnexpectedExceptions = wrapUnexpectedExceptions;
    }

    /**
     * @deprecated use {@link #getJacksonObjectMapperFactory()} instead.
     */
    @Deprecated
    public JacksonConfigureListener getJacksonConfigureListener() {
        return getJacksonObjectMapperFactory();
    }

    /**
     * @deprecated use {@link #setJacksonObjectMapperFactory(JacksonObjectMapperFactory)} instead.
     * @see JacksonConfigureListener
     */
    @Deprecated
    public void setJacksonConfigureListener(final JacksonConfigureListener jacksonConfigureListener) {
        if (jacksonObjectMapperFactory != null) {
            throw new IllegalStateException("Can't have both JacksonObjectMapperFactory and JacksonConfigureListener set. Please use only JacksonObjectMapperFactory.");
        }
        jacksonObjectMapperFactory = new DefaultJacksonObjectMapperFactory() {
            @Override public void configureObjectMapper(ObjectMapper objectMapper) {
                super.configureObjectMapper(objectMapper);
                jacksonConfigureListener.configureObjectMapper(objectMapper);
            }
        };
    }

    /**
     * @return the jacksonObjectMapperFactory
     * @see JacksonObjectMapperFactory
     */
    public JacksonObjectMapperFactory getJacksonObjectMapperFactory() {
        return jacksonObjectMapperFactory;
    }

    /**
     * @param jacksonObjectMapperFactory the jacksonObjectMapperFactory to set
     * @see JacksonObjectMapperFactory
     */
    public void setJacksonObjectMapperFactory(JacksonObjectMapperFactory jacksonObjectMapperFactory) {
        this.jacksonObjectMapperFactory = jacksonObjectMapperFactory;
    }

    public OAuthConsumer getOAuthConsumer() {
        return oAuthConsumer;
    }

    public void setOAuthConsumer(OAuthConsumer oAuthConsumer) {
        this.oAuthConsumer = oAuthConsumer;
    }

}
