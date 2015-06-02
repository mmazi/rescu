package si.mazi.rescu;

import javax.annotation.Nullable;

public interface InvocationAware {

    void setInvocation(@Nullable RestInvocation invocation);

    @Nullable
    RestInvocation getInvocation();
}
