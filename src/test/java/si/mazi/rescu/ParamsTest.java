package si.mazi.rescu;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class ParamsTest  {

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals(
                Params.of().toString(Arrays.asList(BigDecimal.ONE, new BigDecimal("0.00000043"))),
                "1,0.00000043");
    }
}