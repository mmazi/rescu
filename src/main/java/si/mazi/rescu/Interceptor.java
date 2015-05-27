package si.mazi.rescu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public interface Interceptor {
    Object aroundInvoke(InvocationHandler invocationHandler, Object proxy, Method method, Object[] args) throws Throwable;
}
