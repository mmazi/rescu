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

package si.mazi.rescu;

import org.testng.annotations.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author RedDragCZ
 */
public class FormUrlEncodedRequestWriterTest {
    
    public FormUrlEncodedRequestWriterTest() {
    }
    
    /**
     * Test of writeBody method, of class FormUrlEncodedRequestWriter.
     */
    @Test
    public void testWriteBody() {
        FormUrlEncodedRequestWriter writer = new FormUrlEncodedRequestWriter();
        
        HashMap<Class<? extends Annotation>, Params> paramsMap = RestInvocation.createEmptyParamsMap(null);
        paramsMap.put(FormParam.class,
                Params
                    .of("test1", "value& 1")
                    .add("test2", 100));
        
        RestInvocation invocation = new RestInvocation(
                paramsMap,
                new ArrayList<Object>(),
                new RestMethodMetadata(String.class, HttpMethod.GET, null, null, null,
                        RuntimeException.class, MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, null, null, null),
                null, null, null, null, null
            );
        
        String body = writer.writeBody(invocation);
        assertEquals("test1=value%26+1&test2=100", body);
    }
    
}
