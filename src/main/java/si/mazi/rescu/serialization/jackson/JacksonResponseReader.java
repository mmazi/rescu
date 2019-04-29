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

package si.mazi.rescu.serialization.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import si.mazi.rescu.ResponseReader;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Reads the JSON responses into POJO object using Jackson.
 *
 * @author Martin ZIMA
 */
public class JacksonResponseReader extends ResponseReader {

    private final ObjectMapper objectMapper;

    public JacksonResponseReader(ObjectMapper objectMapper, boolean ignoreHttpErrorCodes) {
        super(ignoreHttpErrorCodes);
        this.objectMapper = objectMapper;
    }

    public <T> T read(String httpBody, Type returnType) throws IOException {
        JavaType javaType = objectMapper
                .getTypeFactory()
                .constructType(returnType);

        return objectMapper.readValue(httpBody, javaType);
    }

    @Override
    protected Exception readException(String httpBody, Class<? extends Exception> exceptionType) throws IOException {
        return read(httpBody, exceptionType);
    }

}
