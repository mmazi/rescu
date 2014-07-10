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
import si.mazi.rescu.InvocationResult;
import si.mazi.rescu.ResponseReader;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Reads the JSON responses into POJO object using Jackson.
 *
 * @author Martin ZIMA
 */
public class JacksonResponseReader extends ResponseReader {

    private final JacksonMapper jacksonMapper;

    public JacksonResponseReader(JacksonMapper jacksonMapper, boolean ignoreHttpErrorCodes) {
        super(ignoreHttpErrorCodes);
        this.jacksonMapper = jacksonMapper;
    }

    public <T> T read(InvocationResult invocationResult, Type returnType) throws IOException {
        JavaType javaType = jacksonMapper.getObjectMapper()
                .getTypeFactory()
                .constructType(returnType);

        return jacksonMapper.getObjectMapper().readValue(
                invocationResult.getHttpBody(), javaType);
    }

    @Override
    protected RuntimeException readException(InvocationResult invocationResult, Class<? extends RuntimeException> exceptionType) throws IOException {
        return read(invocationResult, exceptionType);
    }

}
