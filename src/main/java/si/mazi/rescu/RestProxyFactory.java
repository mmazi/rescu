/**
 * Copyright (C) 2013 Matija Mazi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package si.mazi.rescu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Matija Mazi
 * @see #createProxy(Class, InvocationHandler, Interceptor...)
 */
public final class RestProxyFactory {

    private RestProxyFactory() { }

    /**
     * Create a proxy implementation of restInterface. The interface must be annotated with jax-rs annotations. Basic support exists for {@link javax.ws.rs.Path}, {@link javax.ws.rs.GET},
     * {@link javax.ws.rs.POST}, {@link javax.ws.rs.QueryParam}, {@link javax.ws.rs.FormParam}, {@link javax.ws.rs.HeaderParam}, {@link javax.ws.rs.PathParam}., {@link javax.ws.rs.PATCH}
     *
     * @param restInterface The interface to implement
     * @param baseUrl       The service base baseUrl
     * @param <I>           The interface to implement
     * @param config        Client configuration
     * @param interceptors  The interceptors that will be able to intercept all proxy method calls
     * @return a proxy implementation of restInterface
     */
    public static <I> I createProxy(Class<I> restInterface, String baseUrl, ClientConfig config, Interceptor... interceptors) {
        return createProxy(restInterface, wrap(new RestInvocationHandler(restInterface, baseUrl, config), interceptors));
    }

    static InvocationHandler wrap(InvocationHandler handler, Interceptor... interceptors) {
        for (Interceptor interceptor : interceptors) {
            handler = new InterceptedInvocationHandler(interceptor, handler);
        }
        return handler;
    }

    public static <I> I createProxy(Class<I> restInterface, String baseUrl) {
        return createProxy(restInterface, baseUrl, null);
    }

    static <I> I createProxy(Class<I> restInterface, InvocationHandler restInvocationHandler, Interceptor... interceptors) {
        Object proxy = Proxy.newProxyInstance(restInterface.getClassLoader(), new Class[]{restInterface}, wrap(restInvocationHandler, interceptors));
        // noinspection unchecked
        return (I) proxy;
    }
}
