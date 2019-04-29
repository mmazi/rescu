/*
 * The MIT License
 *
 * Copyright 2014 Matija Mazi.
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

package si.mazi.rescu.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.ResponseReader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Returns the response body as a string.
 */
public class PlainTextResponseReader extends ResponseReader {
    private static final Logger log = LoggerFactory.getLogger(PlainTextResponseReader.class);

    public PlainTextResponseReader(boolean ignoreHttpErrorCodes) {
        super(ignoreHttpErrorCodes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String read(String httpBody, Type returnType) throws IOException {
        return httpBody;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected Exception readException(String httpBody, Class<? extends Exception> exceptionType) throws IOException {
        final String message = read(httpBody, exceptionType);
        Exception constructedException = null;

        Exception reflectiveOperationException = null;
        try {
            // try constructng the exception with message
            constructedException = exceptionType.getConstructor(String.class).newInstance(message);
        } catch (IllegalAccessException|InstantiationException|InvocationTargetException|NoSuchMethodException e) {
            reflectiveOperationException = e;
        }

        if (reflectiveOperationException != null) {
            reflectiveOperationException = null;

            try {
                // fallback to no-parameter constructor
                constructedException = exceptionType.newInstance();
                log.warn("Cannot construct a {} with message parameter. Ommiting the message, which was: {}", exceptionType, message);
            } catch (IllegalAccessException | InstantiationException e) {
                reflectiveOperationException = e;
            }

            if (reflectiveOperationException != null) {
                log.warn("Cannot construct a {}. Throwing a RuntimeException instead. Main cause: {}", exceptionType, reflectiveOperationException.toString());
                throw new RuntimeException(message);
            }
        }
        return constructedException;
    }
}
