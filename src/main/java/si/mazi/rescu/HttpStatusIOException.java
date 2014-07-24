package si.mazi.rescu;

import java.io.IOException;

public class HttpStatusIOException extends IOException implements HttpStatusException {
    private final InvocationResult invocationResult;

    public HttpStatusIOException(InvocationResult invocationResult) {
        super("HTTP status code was not OK: " + invocationResult.getStatusCode());
        this.invocationResult = invocationResult;
    }

    public int getHttpStatusCode() {
        return invocationResult.getStatusCode();
    }

    public void setHttpStatusCode(int httpStatus) {
        throw new UnsupportedOperationException("Status code should be provided in constructor.");
    }

    public String getHttpBody() {
        return invocationResult.getHttpBody();
    }
}
