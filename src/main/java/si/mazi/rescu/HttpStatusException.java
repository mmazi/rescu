package si.mazi.rescu;

import java.io.InputStream;

/**
 * @author Matija Mazi <br/>
 * @created 5/24/13 8:46 PM
 */
public class HttpStatusException extends HttpException {
    private final int httpStatusCode;
    private final InputStream httpBody;

    public HttpStatusException(String message, int httpStatusCode, InputStream httpBody) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.httpBody = httpBody;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public InputStream getHttpBody() {
        return httpBody;
    }
}
