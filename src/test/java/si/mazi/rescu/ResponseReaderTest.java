package si.mazi.rescu;

import junit.framework.TestCase;
import org.testng.Assert;

import java.io.IOException;

public class ResponseReaderTest extends TestCase {

    public void testFindCause() throws Exception {
        Assert.assertEquals(ResponseReader.findCause(null, NullPointerException.class), null);

        final NullPointerException npe = new NullPointerException();
        Assert.assertEquals(ResponseReader.findCause(npe, NullPointerException.class), npe);
        Assert.assertEquals(ResponseReader.findCause(npe, IllegalArgumentException.class), null);
        Assert.assertEquals(ResponseReader.findCause(npe, Throwable.class), npe);

        final IOException ioe = new IOException(npe);
        Assert.assertEquals(ResponseReader.findCause(ioe, NullPointerException.class), npe);
        Assert.assertEquals(ResponseReader.findCause(ioe, IllegalArgumentException.class), null);
        Assert.assertEquals(ResponseReader.findCause(ioe, IOException.class), ioe);
        Assert.assertEquals(ResponseReader.findCause(ioe, Throwable.class), ioe);
        Assert.assertEquals(ResponseReader.findCause(ioe, RuntimeException.class), npe);
        Assert.assertEquals(ResponseReader.findCause(ioe, Exception.class), ioe);
    }
}