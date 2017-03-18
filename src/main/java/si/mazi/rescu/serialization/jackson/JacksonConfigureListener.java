/*
 * The MIT License
 *
 * Copyright 2014 Martin Zima (reddragcz).
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
 * Provides hooks for additional configuration of the internally used
 * JSON converter of Jackson library.
 * 
 * @author Martin Zima (reddragcz)
 * @deprecated Use {@link JacksonObjectMapperFactory} instead.
 */
@Deprecated
public interface JacksonConfigureListener {
    
    /**
     * Called during the construction of each REST proxy object,
     * after setting the default or implied ObjectMapper properties.
     * For example, the users might want to register modules with
     * nonstandard (de)serializers now.
     * 
     * @param objectMapper  the ObjectMapper to configure.
     */
    void configureObjectMapper(ObjectMapper objectMapper);
}
