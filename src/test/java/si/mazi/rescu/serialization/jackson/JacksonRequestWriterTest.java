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
import si.mazi.rescu.HttpMethod;
import si.mazi.rescu.RestInvocation;
import si.mazi.rescu.RestMethodMetadata;
import si.mazi.rescu.dto.DummyAccountInfo;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;

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
                new JacksonMapper());
        
        DummyAccountInfo dummyAccountInfo = new DummyAccountInfo("mm", "USD", 3);
        ArrayList<Object> unannotatedParams = new ArrayList<Object>();
        unannotatedParams.add(dummyAccountInfo);

        RestInvocation invocation = new RestInvocation(
                RestInvocation.createEmptyParamsMap(null),
                unannotatedParams,
                new RestMethodMetadata(String.class, HttpMethod.GET, null, null, null,
                        RuntimeException.class, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, null, null, null),
                null, null, null, null, null
            );
        
        String json = writer.writeBody(invocation);
        
        assertEquals(json, "{\"username\":\"mm\",\"currency\":\"USD\",\"amount_int\":3}");
    }
    
}
