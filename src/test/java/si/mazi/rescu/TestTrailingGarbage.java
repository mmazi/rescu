package si.mazi.rescu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TestTrailingGarbage {
    @Test
    public  void testTrailingGarbageIgnored() throws Exception{
        ObjectMapper om = RestInvocationHandler.createObjectMapper();
        final Map map = om.readValue("{\"status\":\"success\",\"data\":{\"bought\":0,\"remaining\":\"1\",\"order_id\":\"372351\",\"funds\":{\"usd\":\"0.00000000\",\"eur\":\"0\",\"btc\":\"0.01010606\",\"ltc\":\"0\",\"nmc\":\"0\",\"trc\":\"0\",\"dvc\":\"0\",\"ppc\":\"0\",\"ftc\":\"0\",\"wdc\":\"0\",\"dgc\":\"0\",\"xpm\":\"0\",\"ctb\":\"0\",\"ctl\":\"0\",\"esb\":\"0\",\"esl\":\"0\",\"ggb\":\"0\",\"amb\":\"0\",\"utc\":\"0\"}}}<html><head><title>500 Internal Server Error</title></head><body><h1>Internal Server Error</h1><p><i>Failed to connect to ::1: Network is unreachable</i></p><p></p></body></html>\n", Map.class);
        System.out.println("map = " + map);
        final Object bought = ((Map) map.get("data")).get("bought");
        System.out.println("data.bought = " + bought);
        Assert.assertEquals(bought.toString(), "0");
    }
}
