package si.mazi.rescu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Matija Mazi <br>
 */
class Config {
    public static final String RESCU_PROPERTIES = "rescu.properties";
    
    

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    
    private static final String HTTP_CONN_TIMEOUT = "rescu.http.connTimeoutMillis";

    private static final String HTTP_READ_TIMEOUT = "rescu.http.readTimeoutMillis";

    private static final String PROXY_HOST = "rescu.http.readProxyHost";

    private static final String PROXY_PORT = "rescu.http.readProxyPort";

    private static final String IGNORE_HTTP_ERROR_CODES = "rescu.http.ignoreErrorCodes";

    private static final int httpConnTimeout;

    private static final int httpReadTimeout;

    private static final String proxyHost;

    private static final Integer proxyPort;

    private static final boolean ignoreHttpErrorCodes;

    static {
        Properties dfts = new Properties();
        dfts.setProperty(HTTP_CONN_TIMEOUT, "30000"); //default 30s
        dfts.setProperty(HTTP_READ_TIMEOUT, "30000"); //default 30s

        Properties properties = new Properties(dfts);
        InputStream propsStream = RestProxyFactory.class.getResourceAsStream("/rescu.properties");
        if (propsStream != null) {
            try {
                properties.load(propsStream);
                log.debug("Loaded properties from {}.", RESCU_PROPERTIES);
            } catch (IOException e) {
                throw new RuntimeException("Error reading " + RESCU_PROPERTIES, e);
            }
        }

        httpConnTimeout = Integer.parseInt(properties.getProperty(HTTP_CONN_TIMEOUT));
        httpReadTimeout = Integer.parseInt(properties.getProperty(HTTP_READ_TIMEOUT));
        proxyHost = properties.getProperty(PROXY_HOST);
        String proxyPortStr = properties.getProperty(PROXY_PORT);
        proxyPort = proxyPortStr == null ? null : Integer.parseInt(proxyPortStr);
        final String ignoreErrorCodes = properties.getProperty(IGNORE_HTTP_ERROR_CODES);
        ignoreHttpErrorCodes = ignoreErrorCodes == null ? false : Boolean.parseBoolean(ignoreErrorCodes);

        log.debug("Configuration from rescu.properties:");
        log.debug("httpConnTimeout = {}", httpConnTimeout);
        log.debug("httpReadTimeout = {}", httpReadTimeout);
        log.debug("proxyHost = {}", proxyHost);
        log.debug("proxyPort = {}", proxyPort);
        log.debug("ignoreHttpErrorCodes = {}", ignoreHttpErrorCodes);
    }

    public static int getHttpConnTimeout() {
        return httpConnTimeout;
    }

    public static int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public static String getProxyHost() {
        return proxyHost;
    }

    public static Integer getProxyPort() {
        return proxyPort;
    }

    public static boolean isIgnoreHttpErrorCodes() {
        return ignoreHttpErrorCodes;
    }
}
