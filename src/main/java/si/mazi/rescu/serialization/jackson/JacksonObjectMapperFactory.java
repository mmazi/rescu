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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for Jackson's ObjectMapper
 *
 * @author mrmx
 */
public interface JacksonObjectMapperFactory extends JacksonConfigureListener {

    /**
     * Creates a configured instance of <code>ObjectMapper</code>.
     *
     * @return configured instance of <code>ObjectMapper</code>
     */
    ObjectMapper createObjectMapper();

    /**
     * Allow configuration after <code>ObjectMapper</code> creation. For example, the users
     * might want to register modules with nonstandard (de)serializers now.
     *
     * @param objectMapper the ObjectMapper to configure.
     */
    void configureObjectMapper(ObjectMapper objectMapper);

}
