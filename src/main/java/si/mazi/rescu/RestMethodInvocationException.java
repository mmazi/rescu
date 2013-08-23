package si.mazi.rescu;

/**
 * This is the base for all rescu exceptions.
 *
 * @author Matija Mazi <br/>
 * @created 8/23/13 9:59 AM
 */
public class RestMethodInvocationException extends RuntimeException {
    public RestMethodInvocationException(String message) {
        super(message);
    }

    public RestMethodInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
