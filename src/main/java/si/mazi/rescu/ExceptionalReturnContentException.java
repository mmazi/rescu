package si.mazi.rescu;

/**
 * Throw this exception (eg. in your json model class constructors) to signal that rescu
 * should parse the response body as exception. Declare the exception type on the method.
 */
public class ExceptionalReturnContentException extends RuntimeException {
    public ExceptionalReturnContentException(String message) {
        super(message);
    }
}
