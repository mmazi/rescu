package si.mazi.rescu;

import si.mazi.rescu.serialization.jackson.JacksonConfigureListener;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ClientConfig {

    private final Map<Class<? extends Annotation>, Params> defaultParamsMap = new HashMap<Class<? extends Annotation>, Params>();
    
    private SSLSocketFactory sslSocketFactory = null;
    private HostnameVerifier hostnameVerifier = null;
    private JacksonConfigureListener jacksonConfigureListener = null;
    private int httpConnTimeout;
    private int httpReadTimeout;
    private Integer proxyPort;
    private String proxyHost;
    private boolean ignoreHttpErrorCodes;
    
    public ClientConfig() {
        httpConnTimeout = Config.getHttpConnTimeout();
        httpReadTimeout = Config.getHttpReadTimeout();
        proxyPort = Config.getProxyPort();
        proxyHost = Config.getProxyHost();
        ignoreHttpErrorCodes = Config.isIgnoreHttpErrorCodes();
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

    /**
     * @return the httpConnTimeout
     */
    public int getHttpConnTimeout() {
        return httpConnTimeout;
    }

    /**
     * @param httpConnTimeout the httpConnTimeout to set
     */
    public void setHttpConnTimeout(int httpConnTimeout) {
        this.httpConnTimeout = httpConnTimeout;
    }

    /**
     * @return the httpReadTimeout
     */
    public int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    /**
     * @param httpReadTimeout the httpReadTimeout to set
     */
    public void setHttpReadTimeout(int httpReadTimeout) {
        this.httpReadTimeout = httpReadTimeout;
    }

    /**
     * @return the proxyPort
     */
    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @param proxyHost the proxyHost to set
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * @return the ignoreHttpErrorCodes
     */
    public boolean isIgnoreHttpErrorCodes() {
        return ignoreHttpErrorCodes;
    }

    /**
     * @param ignoreHttpErrorCodes the ignoreHttpErrorCodes to set
     */
    public void setIgnoreHttpErrorCodes(boolean ignoreHttpErrorCodes) {
        this.ignoreHttpErrorCodes = ignoreHttpErrorCodes;
    }

    /**
     * @return the jacksonConfigureListener
     * @see JacksonConfigureListener
     */
    public JacksonConfigureListener getJacksonConfigureListener() {
        return jacksonConfigureListener;
    }

    /**
     * @param jacksonConfigureListener the jacksonConfigureListener to set
     * @see JacksonConfigureListener
     */
    public void setJacksonConfigureListener(JacksonConfigureListener jacksonConfigureListener) {
        this.jacksonConfigureListener = jacksonConfigureListener;
    }
}
