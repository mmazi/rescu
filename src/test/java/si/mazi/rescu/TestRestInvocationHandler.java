package si.mazi.rescu;

import java.io.IOException;
import java.net.HttpURLConnection;

class TestRestInvocationHandler extends RestInvocationHandler {

    private RestInvocation invocation;
    private final int responseStatusCode;
    private final String responseBody;

    public TestRestInvocationHandler(Class<?> restInterface, ClientConfig config,
                                     String responseBody, int responseStatusCode) {
        super(restInterface, "https://example.com", config);

        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
    }

    @Override
    protected HttpURLConnection invokeHttp(RestInvocation invocation) {
        this.invocation = invocation;
        return null;
    }

    @Override
    protected Object receiveAndMap(RestMethodMetadata methodMetadata, HttpURLConnection connection) throws IOException {
        InvocationResult invocationResult = new InvocationResult(getResponseBody(), getResponseStatusCode());
        return mapInvocationResult(invocationResult, methodMetadata);
    }

    public RestInvocation getInvocation() {
        return invocation;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
