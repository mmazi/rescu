package si.mazi.rescu;

/**
 * @author Matija Mazi <br/>
 * @created 5/24/13 8:46 PM
 */
public class HttpStatusException extends HttpException {
    private final int httpStatusCode;
    private final String httpBody;

    public HttpStatusException(String message, int httpStatusCode, String httpBody) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.httpBody = httpBody;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getHttpBody() {
        return httpBody;
    }
}
