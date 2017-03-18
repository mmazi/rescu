/*
 * The MIT License
 *
 * Copyright 2014 RedDragCZ.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package si.mazi.rescu;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Interface for deserializing of REST returned data.
 * 
 * @author Martin ZIMA
 */
public abstract class ResponseReader {

    private static final Logger log = LoggerFactory.getLogger(ResponseReader.class);
    public static final int BODY_FRAGMENT_CHARS = 200;

    private final boolean ignoreHttpErrorCodes;

    public ResponseReader(boolean ignoreHttpErrorCodes) {
        this.ignoreHttpErrorCodes = ignoreHttpErrorCodes;
    }

    public boolean isIgnoreHttpErrorCodes() {
        return ignoreHttpErrorCodes;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public Object read(InvocationResult invocationResult, RestMethodMetadata methodMetadata)
            throws IOException {
        final String httpBody = invocationResult.getHttpBody();
        Exception normalParseFailCause = null;
        final boolean isHttpStatusPass = !invocationResult.isErrorStatusCode() || isIgnoreHttpErrorCodes();
        if (isHttpStatusPass) {
            if (httpBody == null || httpBody.length() == 0) {
                return null;
            } else {
                try {
                    return read(httpBody, methodMetadata.getReturnType());
                } catch (IOException|RuntimeException e) {
                    normalParseFailCause = findCause(e, ExceptionalReturnContentException.class, JsonMappingException.class);
                    if (normalParseFailCause == null) {
                        throw e;
                    }
                }
                log.debug("Parsing response as {} failed: {}", methodMetadata.getReturnType(), normalParseFailCause.toString());
            }
        }

        // We shoud throw an exception now.

        if (methodMetadata.getExceptionType() != null && httpBody != null) {
            // Try with the declared custom exception first (methodMetadata.getExceptionType()).
            RuntimeException exception = null;
            try {
                exception = readException(httpBody, methodMetadata.getExceptionType());
            } catch (Exception e) {
                log.warn("Noncritical error parsing error output: " + Utils.clip(httpBody, BODY_FRAGMENT_CHARS), e);
            }

            if (exception != null) {
                if (exception.getMessage() == null) {
                    log.info("Constructed an exception with no message. Response body was: {}", Utils.clip(httpBody, BODY_FRAGMENT_CHARS));
                }
                if (exception instanceof HttpStatusException) {
                    ((HttpStatusException) exception).setHttpStatusCode(invocationResult.getStatusCode());
                }
                throw exception;
            }
        }

        String exceptionMessage = normalParseFailCause instanceof ExceptionalReturnContentException
                ? String.format("Response body could not be parsed as method return type %s: %s", methodMetadata.getReturnType(), normalParseFailCause.getMessage())
                : isHttpStatusPass
                    ? normalParseFailCause.getMessage()
                    : "HTTP status code was not OK: " + invocationResult.getStatusCode();

        throw new HttpStatusIOException(exceptionMessage, invocationResult);
    }

    protected abstract <T> T read(String httpBody, Type returnType) throws IOException, ExceptionalReturnContentException;

    protected abstract RuntimeException readException(String httpBody, Class<? extends RuntimeException> exceptionType) throws IOException;

    /**
     * Search the cause chain of <em>t</em> (starting from and including <em>t</em>) for a Throwable
     * that is assignable to any of <em>ofClasses</em>, or null if not found. The chain is first fully searched
     * for the first of <em>C</em>, then second, etc.
     *
     * @param ofClasses Throwable classes in order of priority to be searched for.
     * @param root      The cause from which the search is to be started.
     * @param <T>       This should be figured out by the compiler: the lowest common ancestor of <em>ofClasses</em>.
     * @param <C>       Should be figured out by the compiler too.
     */
    @SafeVarargs
    public static <T extends Throwable, C extends Class<? extends T>> T findCause(Throwable root, C... ofClasses) {
        for (C c : ofClasses) {
            Throwable t = root;
            while (t != null) {
                if (c.isInstance(t)) {
                    return c.cast(t);
                }
                t = t.getCause();
            }
        }
        return null;
    }
}
