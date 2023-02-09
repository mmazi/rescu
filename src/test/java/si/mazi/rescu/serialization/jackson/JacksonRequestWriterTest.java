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

import org.testng.annotations.Test;
import si.mazi.rescu.RestInvocation;
import si.mazi.rescu.RestMethodMetadata;
import si.mazi.rescu.dto.DummyAccountInfo;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static si.mazi.rescu.HttpMethod.GET;

/**
 *
 * @author RedDragCZ
 */
public class JacksonRequestWriterTest {
    
    public JacksonRequestWriterTest() {
    }

    /**
     * Test of writeBody method, of class JacksonRequestWriter.
     */
    @Test
    public void testWriteBody() {
        JacksonRequestWriter writer = new JacksonRequestWriter(
                new DefaultJacksonObjectMapperFactory().createObjectMapper());
        
        DummyAccountInfo dummyAccountInfo = new DummyAccountInfo("mm", "USD", 3);

        RestInvocation invocation = RestInvocation.create(
                null,
                new RestMethodMetadata(String.class, GET, null, null, null,
                        RuntimeException.class, APPLICATION_JSON, APPLICATION_JSON, null, new HashMap<>(), new Annotation[][]{{}}),
                new Object[]{dummyAccountInfo},
                null
        );

        String json = writer.writeBody(invocation);
        
        assertEquals(json, "{\"username\":\"mm\",\"currency\":\"USD\",\"amount_int\":3}");
    }
    
}
