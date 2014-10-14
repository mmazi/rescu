package si.mazi.rescu;

import java.io.IOException;

public class ResponseException extends IOException implements HttpStatusException {
    private final InvocationResult invocationResult;

    public ResponseException(String message, InvocationResult invocationResult) {
        super(message);
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
