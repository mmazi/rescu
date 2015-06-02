package si.mazi.rescu;

import javax.annotation.Nullable;

public class InvocationAwareException extends RuntimeException implements InvocationAware {

    @Nullable
    private RestInvocation invocation;

    public InvocationAwareException(Exception e, RestInvocation invocation) {
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
}
