package si.mazi.rescu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Matija Mazi <br/>
 * @created 5/11/13 1:28 PM
 */
class Config {
    public static final String RESCU_PROPERTIES = "rescu.properties";

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private static final String HTTP_READ_TIMEOUT = "rescu.http.readTimeoutMillis";

    private static final String PROXY_HOSTNAME = "rescu.http.readProxyHostname";

    private static final String PROXY_PORT = "rescu.http.readProxyPort";

    private static final int httpReadTimeout;

    private static final String proxyHostname;

    private static final Integer proxyPort;

    static {
        Properties dfts = new Properties();
        dfts.setProperty(HTTP_READ_TIMEOUT, "30000");

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

        httpReadTimeout = Integer.parseInt(properties.getProperty(HTTP_READ_TIMEOUT));
        proxyHostname = properties.getProperty(PROXY_HOSTNAME);
        String proxyPortStr = properties.getProperty(PROXY_PORT);
        proxyPort = proxyPortStr == null ? null : Integer.parseInt(proxyPortStr);

        log.debug("Configuration from rescu.properties:");
        log.debug("httpReadTimeout = {}", httpReadTimeout);
        log.debug("proxyHostname = {}", proxyHostname);
        log.debug("proxyPort = {}", proxyPort);
    }

    public static int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public static String getProxyHostname() {
        return proxyHostname;
    }

    public static Integer getProxyPort() {
        return proxyPort;
    }
}
