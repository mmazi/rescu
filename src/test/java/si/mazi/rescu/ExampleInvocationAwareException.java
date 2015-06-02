package si.mazi.rescu;

import javax.annotation.Nullable;

/**
 * @author Matija Mazi <br>
 */
public class ExampleInvocationAwareException extends HttpStatusExceptionSupport implements InvocationAware {
    @Nullable
    private RestInvocation invocation;

    @Override
    public void setInvocation(@Nullable RestInvocation invocation) {

        this.invocation = invocation;
    }

    @Override
    @Nullable
    public RestInvocation getInvocation() {
        return invocation;
    }
}
