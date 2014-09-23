package si.mazi.rescu;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HttpStatusExceptionSupport extends RuntimeException implements HttpStatusException {

    @JsonIgnore
    private int __httpStatusCode;

    public HttpStatusExceptionSupport() { }

    public HttpStatusExceptionSupport(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return String.format("%s (HTTP status code: %d)", super.getMessage(), __httpStatusCode);
    }

    public int getHttpStatusCode() {
        return __httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.__httpStatusCode = httpStatusCode;
    }
}
