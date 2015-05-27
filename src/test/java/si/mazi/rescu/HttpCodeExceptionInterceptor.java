package si.mazi.rescu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class HttpCodeExceptionInterceptor implements Interceptor {
    @Override
    public Object aroundInvoke(InvocationHandler invocationHandler, Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return invocationHandler.invoke(proxy, method, args);
        } catch (Throwable throwable) {
            if (throwable instanceof HttpStatusException) {
                switch (((HttpStatusException) throwable).getHttpStatusCode()) {
                    case 500: throw new Http500Exception();
                }
            }
            throw throwable;
        }
    }
}
