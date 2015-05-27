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