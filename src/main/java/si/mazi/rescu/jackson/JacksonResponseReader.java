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

package si.mazi.rescu.jackson;

import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.HttpStatusException;
import si.mazi.rescu.InvocationResult;
import si.mazi.rescu.ResponseReader;
import si.mazi.rescu.RestMethodMetadata;

import java.io.IOException;

/**
 * Reads the JSON responses into POJO object using Jackson.
 *
 * @author Martin ZIMA
 */
public class JacksonResponseReader implements ResponseReader {

    private final Logger logger = LoggerFactory.getLogger(JacksonResponseReader.class);

    private final JacksonMapper jacksonMapper;

    private final boolean ignoreHttpErrorCodes;

    public JacksonResponseReader(JacksonMapper jacksonMapper, boolean ignoreHttpErrorCodes) {
        this.jacksonMapper = jacksonMapper;
        this.ignoreHttpErrorCodes = ignoreHttpErrorCodes;
    }

    public Object read(InvocationResult invocationResult, RestMethodMetadata methodMetadata)
            throws IOException {
        if (!invocationResult.isErrorStatusCode() || ignoreHttpErrorCodes) {
            if (invocationResult.getHttpBody() == null || invocationResult.getHttpBody().isEmpty()) {
                return null;
            } else {
                JavaType javaType = jacksonMapper.getObjectMapper()
                        .getTypeFactory()
                        .constructType(methodMetadata.getReturnType());

                return jacksonMapper.getObjectMapper().readValue(
                        invocationResult.getHttpBody(), javaType);
            }
        } else {
            if (methodMetadata.getExceptionType() != null) {
                RuntimeException exception = null;
                try {
                    exception = jacksonMapper.getObjectMapper().readValue(invocationResult.getHttpBody(),
                            methodMetadata.getExceptionType());
                } catch (IOException e) {
                    logger.warn("Error parsing error output: " + e.toString());
                }

                if (exception != null) {
                    if (exception instanceof HttpStatusException) {
                        ((HttpStatusException) exception).setHttpStatusCode(invocationResult.getStatusCode());
                    }
                    throw exception;
                }
            }

            throw new IOException(String.format("HTTP status code was %d; response body: %s",
                    invocationResult.getStatusCode(), invocationResult.getHttpBody()));
        }

    }

}
