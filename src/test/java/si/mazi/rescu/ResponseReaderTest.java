/*
 * Copyright (C) 2015 Matija Mazi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package si.mazi.rescu;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static si.mazi.rescu.ResponseReader.findCause;

public class ResponseReaderTest  {

    @Test
    public void testFindCause() throws Exception {
        final NullPointerException cause = findCause(null, NullPointerException.class);
        Assert.assertEquals(cause, null);

        final NullPointerException npe = new NullPointerException();
        Assert.assertEquals(findCause(npe, NullPointerException.class), npe);
        Assert.assertEquals(findCause(npe, IllegalArgumentException.class), null);
        Assert.assertEquals(findCause(npe, Throwable.class), npe);

        final IOException ioe = new IOException(npe);
        Assert.assertEquals(findCause(ioe, NullPointerException.class), npe);
        Assert.assertEquals(findCause(ioe, IllegalArgumentException.class), null);
        Assert.assertEquals(findCause(ioe, IOException.class), ioe);
        Assert.assertEquals(findCause(ioe, Throwable.class), ioe);
        Assert.assertEquals(findCause(ioe, RuntimeException.class), npe);
        Assert.assertEquals(findCause(ioe, Exception.class), ioe);

        ///////////// Several exception types

        Assert.assertEquals(findCause(npe, NullPointerException.class), npe);
        Assert.assertEquals(findCause(npe, IllegalArgumentException.class), null);
        Assert.assertEquals(findCause(npe, IllegalArgumentException.class, NullPointerException.class), npe);

        Assert.assertEquals(findCause(ioe, NullPointerException.class), npe);
        Assert.assertEquals(findCause(ioe, IllegalArgumentException.class), null);
        Assert.assertEquals(findCause(ioe, IOException.class, NullPointerException.class), ioe);
        Assert.assertEquals(findCause(ioe, NullPointerException.class, IOException.class), npe);
        Assert.assertEquals(findCause(ioe, RuntimeException.class, NullPointerException.class), npe);
        Assert.assertEquals(findCause(ioe, NullPointerException.class, RuntimeException.class), npe);
        Assert.assertEquals(findCause(ioe, RuntimeException.class, Throwable.class), npe);
        Assert.assertEquals(findCause(ioe, Throwable.class, RuntimeException.class), ioe);

        Assert.assertEquals(findCause(ioe, IllegalArgumentException.class, UnsupportedOperationException.class), null);
    }
}