/*
 * The MIT License
 *
 * Copyright 2016 mrmx.
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Default implementation for JacksonObjectMapperFactory
 *
 * @author mrmx
 */
public class DefaultJacksonObjectMapperFactory implements JacksonObjectMapperFactory {

    /**
     * Creates a configured instance of <code>ObjectMapper</code>.
     *
     * @return configured instance of <code>ObjectMapper</code>
     */
    @Override
    public ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = createInstance();
        configureObjectMapper(objectMapper);
        return objectMapper;
    }

    @Override
    public void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.setAnnotationIntrospector(new IgnoreThrowableProperties());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    }

    /**
     * Allows subclasing and overriding of ObjectMapper instances
     *
     * @return instance of <code>ObjectMapper</code>
     */
    protected ObjectMapper createInstance() {
        return new ObjectMapper();
    }

}
