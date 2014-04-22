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
package si.mazi.rescu.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Scanner;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;
import si.mazi.rescu.ExampleException;
import si.mazi.rescu.HttpMethod;
import si.mazi.rescu.InvocationResult;
import si.mazi.rescu.ResourceUtils;
import si.mazi.rescu.RestInvocationHandler;
import si.mazi.rescu.RestMethodMetadata;
import si.mazi.rescu.dto.DummyTicker;
import si.mazi.rescu.dto.GenericResult;

/**
 *
 * @author RedDragCZ
 */
public class JacksonResponseReaderTest {

    public JacksonResponseReaderTest() {
    }

    /**
     * Test of read method, of class JacksonResponseReader.
     */
    @Test
    public void testRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(
                new JacksonMapper(null), true);

        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/example-ticker.json"), 200);

        Object result = reader.read(invocationResult,
                new RestMethodMetadata(DummyTicker.class, HttpMethod.GET, null, null, null,
                        RuntimeException.class, null, null, null, null));
        
        assertEquals(DummyTicker.class, result.getClass());
        assertEquals(34567L, ((DummyTicker)result).getVolume());
    }
    
    @Test
    public void testExceptionRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(
                new JacksonMapper(null), false);

        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/error.json"), 500);

        try {
            Object result = reader.read(invocationResult,
                new RestMethodMetadata(DummyTicker.class, HttpMethod.GET, null, null, null,
                        ExampleException.class, null, null, null, null));
            
            Assert.assertTrue(false, "An exception should have been thrown.");
        } catch (ExampleException e) {
            Assert.assertEquals(e.getError(), "Order not found");
            Assert.assertEquals(e.getToken(), "unknown_error");
            Assert.assertEquals(e.getResult(), "error");
        } catch (Exception e) {
            Assert.assertTrue(false, "Wrong exception type thrown: " + e);
        }
    }
    
    @Test
    public void testGenericRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(
                new JacksonMapper(null), true);

        Type resType = new TypeReference<GenericResult<DummyTicker[]>>() {}.getType();
        
        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/example-generic.json"), 200);

        Object result = reader.read(invocationResult,
                new RestMethodMetadata(resType, HttpMethod.GET, null, null, null,
                        RuntimeException.class, null, null, null, null));
        
        assertEquals(GenericResult.class, result.getClass());
        assertNotNull(((GenericResult)result).getResult());
        assertEquals(DummyTicker[].class, ((GenericResult)result).getResult().getClass());
        
        DummyTicker[] tickers = (DummyTicker[])((GenericResult)result).getResult();
        assertEquals(2, tickers.length);
        assertEquals(12345, tickers[0].getLast());
        assertEquals(8910, tickers[1].getVolume());
    }

    @Test
    public void testTrailingGarbageIgnored() throws Exception{
        JacksonResponseReader reader = new JacksonResponseReader(
                new JacksonMapper(null), true);
        
        InvocationResult invocationResult = new InvocationResult(
                "{\"status\":\"success\",\"data\":{\"bought\":0,\"remaining\":\"1\",\"order_id\":\"372351\",\"funds\":{\"usd\":\"0.00000000\",\"eur\":\"0\",\"btc\":\"0.01010606\",\"ltc\":\"0\",\"nmc\":\"0\",\"trc\":\"0\",\"dvc\":\"0\",\"ppc\":\"0\",\"ftc\":\"0\",\"wdc\":\"0\",\"dgc\":\"0\",\"xpm\":\"0\",\"ctb\":\"0\",\"ctl\":\"0\",\"esb\":\"0\",\"esl\":\"0\",\"ggb\":\"0\",\"amb\":\"0\",\"utc\":\"0\"}}}<html><head><title>500 Internal Server Error</title></head><body><h1>Internal Server Error</h1><p><i>Failed to connect to ::1: Network is unreachable</i></p><p></p></body></html>\n",
                200);
        
        Object result = reader.read(invocationResult,
                new RestMethodMetadata(Map.class, HttpMethod.GET, null, null, null,
                        RuntimeException.class, null, null, null, null));
        
        assert(Map.class.isAssignableFrom(result.getClass()));
        Map map = (Map)result;
        
        final Object bought = ((Map) map.get("data")).get("bought");
        Assert.assertEquals(bought.toString(), "0");
    }
}
