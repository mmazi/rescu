package si.mazi.rescu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AwareException extends RuntimeException implements InvocationAware, HttpResponseAware {

    @Nullable
    private RestInvocation invocation;
    private Map<String, List<String>> responseHeaders;

    public AwareException(Exception e, RestInvocation invocation) {
        super(e);
        this.invocation = invocation;
    }

    @Override
    public void setInvocation(@Nullable RestInvocation invocation) {
        this.invocation = invocation;
    }

    @Nullable
    public RestInvocation getInvocation() {
        return invocation;
    }

    @Override
    public void setResponseHeaders(Map<String, List<String>> headers) {
        this.responseHeaders = headers;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }
}
