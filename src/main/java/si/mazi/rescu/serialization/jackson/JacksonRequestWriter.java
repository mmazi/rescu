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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import si.mazi.rescu.RequestWriter;
import si.mazi.rescu.RestInvocation;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

/**
 * Writes the data as JSON-serialized string using Jackson.
 * 
 * @author Martin ZIMA
 */
public class JacksonRequestWriter implements RequestWriter {

    private final ObjectMapper objectMapper;

    public JacksonRequestWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String writeBody(RestInvocation invocation) {
        if (!MediaType.APPLICATION_JSON.equals(invocation.getMethodMetadata().getReqContentType())) {
            throw new IllegalArgumentException("JsonRequestWriter supports application/json content type only!");
        }
        
        if (invocation.getParamsMap().get(FormParam.class) != null
                && !invocation.getParamsMap().get(FormParam.class).isEmpty()) {
            throw new IllegalArgumentException("@FormParams are not allowed with " + MediaType.APPLICATION_JSON);
        } else if (invocation.getUnannanotatedParams().size() > 1) {
            throw new IllegalArgumentException("Can only have a single unannotated parameter with " + MediaType.APPLICATION_JSON);
        }
        
        if (invocation.getUnannanotatedParams().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(invocation.getUnannanotatedParams().get(0));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing json. This could be due to an error in your Jackson mapping, or a bug in rescu.", e);
        }
    }

}
