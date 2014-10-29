package si.mazi.rescu;

import junit.framework.TestCase;
import org.testng.Assert;

public class UtilsTest extends TestCase {

    public void testClip() throws Exception {
        Assert.assertEquals(Utils.clip("12345", 3), "123...");
        Assert.assertEquals(Utils.clip("123", 3), "123");
        Assert.assertEquals(Utils.clip("12", 3), "12");
        Assert.assertEquals(Utils.clip("", 3), "");
        Assert.assertEquals(Utils.clip("", 0), "");
        Assert.assertEquals(Utils.clip("234", 0), "...");
    }
}