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

import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * Resolves which RequestWriter to use for REST requests.
 * 
 * @author Martin ZIMA
 */
public class RequestWriterResolver {

    //media-type => request writer
    protected final HashMap<String, RequestWriter> writers = new HashMap<>();
    
    public RequestWriterResolver() {
    }
    
    public void addWriter(String mediaType, RequestWriter writer) {
        writers.put(mediaType, writer);
    }
    
    public RequestWriter resolveWriter(RestMethodMetadata methodMetadata) {
        RequestWriter writer;

        String reqContentType = methodMetadata.getReqContentType();
        if (reqContentType == null) {
            //throw new IllegalArgumentException("No media type specified; don't know how to create request body. Please specify the body media type using @javax.ws.rs.Consumes.");
            reqContentType = MediaType.APPLICATION_FORM_URLENCODED;
        }
        writer = writers.get(reqContentType);
        if (writer == null) {
            throw new IllegalArgumentException("Unsupported media type: " + reqContentType);
        }

        return writer;
    }
    
}
