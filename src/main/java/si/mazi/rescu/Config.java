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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Matija Mazi <br>
 */
final class Config {
    public static final String RESCU_PROPERTIES = "rescu.properties";
    
    

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    
    private static final String HTTP_CONN_TIMEOUT = "rescu.http.connTimeoutMillis";

    private static final String HTTP_READ_TIMEOUT = "rescu.http.readTimeoutMillis";

    private static final String PROXY_HOST = "rescu.http.readProxyHost";

    private static final String PROXY_PORT = "rescu.http.readProxyPort";

    private static final String IGNORE_HTTP_ERROR_CODES = "rescu.http.ignoreErrorCodes";

    private static final String WRAP_UNEXPECTED_EXCEPTIONS = "rescu.http.wrapUnexpectedExceptions";

    private static final int httpConnTimeout;

    private static final int httpReadTimeout;

    private static final String proxyHost;

    private static final Integer proxyPort;

    private static final boolean ignoreHttpErrorCodes;

    private static final boolean wrapUnexpectedExceptions;

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
        ignoreHttpErrorCodes = getBoolean(properties, IGNORE_HTTP_ERROR_CODES);
        wrapUnexpectedExceptions = getBoolean(properties, WRAP_UNEXPECTED_EXCEPTIONS);

        log.debug("Configuration from rescu.properties:");
        log.debug("httpConnTimeout = {}", httpConnTimeout);
        log.debug("httpReadTimeout = {}", httpReadTimeout);
        log.debug("proxyHost = {}", proxyHost);
        log.debug("proxyPort = {}", proxyPort);
        log.debug("ignoreHttpErrorCodes = {}", ignoreHttpErrorCodes);
    }

    private Config() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    private static boolean getBoolean(Properties properties, String key) {
        final String ignoreErrorCodes = properties.getProperty(key);
        return ignoreErrorCodes != null && Boolean.parseBoolean(ignoreErrorCodes);
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

    public static boolean isWrapUnexpectedExceptions() {
        return wrapUnexpectedExceptions;
    }
}
