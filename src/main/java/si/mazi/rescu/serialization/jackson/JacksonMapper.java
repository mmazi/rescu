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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides Jackson object mapping services.
 * 
 * @author Martin ZIMA
 */
public class JacksonMapper {

    private final JacksonConfigureListener jacksonConfigureListener;
    private final JacksonObjectMapperFactory jacksonObjectMapperFactory;
    private final ObjectMapper objectMapper;
        
    public JacksonMapper(JacksonConfigureListener jacksonConfigureListener) {
        this(jacksonConfigureListener,null);
    }
    
    public JacksonMapper(JacksonConfigureListener jacksonConfigureListener,JacksonObjectMapperFactory jacksonObjectMapperFactory) {
        this.jacksonConfigureListener = jacksonConfigureListener;                        
        if(jacksonObjectMapperFactory == null) {
           jacksonObjectMapperFactory = new DefaultJacksonObjectMapperFactory();
        }
        this.jacksonObjectMapperFactory = jacksonObjectMapperFactory;        
        this.objectMapper = createObjectMapper();
        if (this.jacksonConfigureListener != null) {
            this.jacksonConfigureListener.configureObjectMapper(objectMapper);
        }
    }
    
    protected ObjectMapper createObjectMapper() {
        return jacksonObjectMapperFactory.createObjectMapper();
    }
    
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
