package si.mazi.rescu;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UtilsTest {

    @Test
    public void testClip() throws Exception {
        Assert.assertEquals(Utils.clip("12345", 3), "123...");
        Assert.assertEquals(Utils.clip("123", 3), "123");
        Assert.assertEquals(Utils.clip("12", 3), "12");
        Assert.assertEquals(Utils.clip("", 3), "");
        Assert.assertEquals(Utils.clip("", 0), "");
        Assert.assertEquals(Utils.clip("234", 0), "...");
    }
}