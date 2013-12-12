/*
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

/**
 * <p>
 * Classes in this package may be used to create proxy client objects for REST web services.
 * </p>
 * <p>
 * For a working example see the XChange project on github.
 * </p>
 * <p>
 * This is based on JAX-RS ({@link javax.ws.rs} -- only the JAX-RS annotations are used) and {@link java.lang.reflect.Proxy}.
 * </p>
 *
 * Usage:
 * <ol>
 *   <li>Create a java interface that represents the API -- see BitStamp.java for an example</li>
 *   <li>Annotate the interface with JAX-RS annotations</li>
 *   <li>Call {@link si.mazi.rescu.RestProxyFactory#createProxy(Class, String)}</li>
 * </ol>
 * <p>
 * Basic support is provided for {@link javax.ws.rs.Path}, {@link javax.ws.rs.GET}, {@link javax.ws.rs.POST},
 * {@link javax.ws.rs.DELETE}, {@link javax.ws.rs.HEAD}, {@link javax.ws.rs.OPTIONS}, {@link javax.ws.rs.QueryParam},
 * {@link javax.ws.rs.FormParam}, {@link javax.ws.rs.HeaderParam}, {@link javax.ws.rs.PathParam}.
 * </p>
 *
 * Some of the classes here may also be useful even when not using the JAX-RS approach to calling REST web services,
 * eg. {@link si.mazi.rescu.Params}.
 *
 * @see si.mazi.rescu.RestProxyFactory
 *
 */
package si.mazi.rescu;