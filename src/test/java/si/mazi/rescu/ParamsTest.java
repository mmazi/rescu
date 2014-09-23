package si.mazi.rescu;

import junit.framework.TestCase;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.Arrays;

public class ParamsTest extends TestCase {

    public void testToString() throws Exception {
        Assert.assertEquals(
                Params.toString(Arrays.asList(BigDecimal.ONE, new BigDecimal("0.00000043"))),
                "1,0.00000043");
    }
}