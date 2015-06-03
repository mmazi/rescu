package si.mazi.rescu;

import java.util.List;
import java.util.Map;

/**
 * @author Matija Mazi <br>
 */
public class ExampleResponseHeadersAwareException extends HttpStatusExceptionSupport implements HttpResponseAware {

    private Map<String, List<String>> headers;

    @Override
    public void setResponseHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return headers;
    }
}
