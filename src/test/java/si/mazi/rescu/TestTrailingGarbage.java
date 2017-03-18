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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TestTrailingGarbage {
    @Test
    public  void testTrailingGarbageIgnored() throws Exception{
        ObjectMapper om = new ObjectMapper(null);
        final Map map = om.readValue("{\"status\":\"success\",\"data\":{\"bought\":0,\"remaining\":\"1\",\"order_id\":\"372351\",\"funds\":{\"usd\":\"0.00000000\",\"eur\":\"0\",\"btc\":\"0.01010606\",\"ltc\":\"0\",\"nmc\":\"0\",\"trc\":\"0\",\"dvc\":\"0\",\"ppc\":\"0\",\"ftc\":\"0\",\"wdc\":\"0\",\"dgc\":\"0\",\"xpm\":\"0\",\"ctb\":\"0\",\"ctl\":\"0\",\"esb\":\"0\",\"esl\":\"0\",\"ggb\":\"0\",\"amb\":\"0\",\"utc\":\"0\"}}}<html><head><title>500 Internal Server Error</title></head><body><h1>Internal Server Error</h1><p><i>Failed to connect to ::1: Network is unreachable</i></p><p></p></body></html>\n", Map.class);
        System.out.println("map = " + map);
        final Object bought = ((Map) map.get("data")).get("bought");
        System.out.println("data.bought = " + bought);
        Assert.assertEquals(bought.toString(), "0");
    }
}
