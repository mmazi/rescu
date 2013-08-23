package si.mazi.rescu;

/**
 * @author Matija Mazi <br/>
 * @created 8/23/13 10:00 AM
 */
public class RestJsonException extends RestMethodInvocationException {
    public RestJsonException(String message) {
        super(message);
    }

    public RestJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
