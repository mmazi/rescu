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

    private final boolean ignoreHttpErrorCodes;

    public ResponseReader(boolean ignoreHttpErrorCodes) {
        this.ignoreHttpErrorCodes = ignoreHttpErrorCodes;
    }

    public boolean isIgnoreHttpErrorCodes() {
        return ignoreHttpErrorCodes;
    }

    public Object read(InvocationResult invocationResult, RestMethodMetadata methodMetadata)
            throws IOException {
        final String httpBody = invocationResult.getHttpBody();
        if (!invocationResult.isErrorStatusCode() || isIgnoreHttpErrorCodes()) {
            if (httpBody == null || httpBody.length() == 0) {
                return null;
            } else {
                return read(httpBody, methodMetadata.getReturnType());
            }
        } else {
            if (methodMetadata.getExceptionType() != null && httpBody != null) {
                RuntimeException exception = null;
                try {
                    exception = readException(httpBody, methodMetadata.getExceptionType());
                } catch (IOException e) {
                    log.warn("Error parsing error output: " + e.toString());
                }

                if (exception != null) {
                    if (exception instanceof HttpStatusException) {
                        ((HttpStatusException) exception).setHttpStatusCode(invocationResult.getStatusCode());
                    }
                    throw exception;
                }
            }

            throw new HttpStatusIOException(invocationResult);
        }

    }

    protected abstract <T> T read(String httpBody, Type returnType) throws IOException;

    protected abstract RuntimeException readException(String httpBody, Class<? extends RuntimeException> exceptionType) throws IOException;
}
