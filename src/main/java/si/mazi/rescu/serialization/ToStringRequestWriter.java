/*
 * The MIT License
 *
 * Copyright 2014 Matija Mazi.
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
package si.mazi.rescu.serialization;

import si.mazi.rescu.RequestWriter;
import si.mazi.rescu.RestInvocation;

import javax.ws.rs.FormParam;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Writes the data as string using toString.
 */
public class ToStringRequestWriter implements RequestWriter {

    public String writeBody(RestInvocation invocation) {
        if (!TEXT_PLAIN.equals(invocation.getMethodMetadata().getReqContentType())) {
            throw new IllegalArgumentException("ToStringRequestWriter supports " + TEXT_PLAIN + " content type only!");
        }
        
        if (invocation.getParamsMap().get(FormParam.class) != null
                && !invocation.getParamsMap().get(FormParam.class).isEmpty()) {
            throw new IllegalArgumentException("@FormParams are not allowed with " + TEXT_PLAIN);
        } else if (invocation.getUnannanotatedParams().size() > 1) {
            throw new IllegalArgumentException("Can only have a single unannotated parameter with " + TEXT_PLAIN);
        }
        
        if (invocation.getUnannanotatedParams().isEmpty()) {
            return null;
        }
        
        return invocation.getUnannanotatedParams().get(0).toString();
    }
}
