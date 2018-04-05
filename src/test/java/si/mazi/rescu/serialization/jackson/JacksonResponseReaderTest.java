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
package si.mazi.rescu.serialization.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import si.mazi.rescu.*;
import si.mazi.rescu.dto.DummyTicker;
import si.mazi.rescu.dto.GenericResult;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author RedDragCZ
 */
public class JacksonResponseReaderTest {

    private static final Logger log = LoggerFactory.getLogger(JacksonResponseReaderTest.class);

    public JacksonResponseReaderTest() {
    }

    /**
     * Test of read method, of class JacksonResponseReader.
     */
    @Test
    public void testRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(createObjectMapper(), true);

        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/example-ticker.json"), 200);

        Object result = reader.read(invocationResult,
                new RestMethodMetadata(DummyTicker.class, HttpMethod.GET, null, null, null,
                        RuntimeException.class, null, MediaType.APPLICATION_JSON, null, null, null));
        
        assertThat(result).isInstanceOf(DummyTicker.class);
        assertThat(((DummyTicker) result).getVolume()).isEqualTo(34567L);
    }
    
    @Test
    public void testExceptionRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(createObjectMapper(), false);

        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/error.json"), 500);

        ExampleException e = ExceptionUtils.catchException(ExampleException.class,
                () -> reader.read(invocationResult, new RestMethodMetadata(DummyTicker.class, HttpMethod.GET, null, null, null,
                        ExampleException.class, null, MediaType.APPLICATION_JSON, null, null, null))
        );
        assertThat(e.getError()).isEqualTo("Order not found");
        assertThat(e.getToken()).isEqualTo("unknown_error");
        assertThat(e.getResult()).isEqualTo("error");
        assertThat(e.getHttpStatusCode()).isEqualTo(500);
    }

    @Test
    public void testIOExceptionRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(createObjectMapper(), false);

        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/error.json"), 500);

        HttpStatusIOException e = ExceptionUtils.catchException(HttpStatusIOException.class,
                () -> reader.read(invocationResult,
                new RestMethodMetadata(DummyTicker.class, HttpMethod.GET, null, null, null,
                        null, null, MediaType.APPLICATION_JSON, null, null, null))
        );

        assertThat(e.getHttpBody()).contains("Order not found");
        assertThat(e.getHttpBody()).contains("unknown_error");
        assertThat(e.getHttpStatusCode()).isEqualTo(500);
    }

    @Test
    public void testGenericRead() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(createObjectMapper(), true);

        Type resType = new TypeReference<GenericResult<DummyTicker[]>>() {}.getType();
        
        InvocationResult invocationResult = new InvocationResult(
                ResourceUtils.getResourceAsString("/example-generic.json"), 200);

        Object result = reader.read(invocationResult,
                new RestMethodMetadata(resType, HttpMethod.GET, null, null, null,
                        RuntimeException.class, null, MediaType.APPLICATION_JSON, null, null, null));

        assertThat(result).isInstanceOf(GenericResult.class);
        assertThat(((GenericResult)result).getResult())
                .isNotNull()
                .isInstanceOf(DummyTicker[].class);
        
        DummyTicker[] tickers = (DummyTicker[])((GenericResult)result).getResult();
        assertThat(tickers).hasSize(2);
        assertThat(tickers[0].getLast()).isEqualTo(12345);
        assertThat(tickers[1].getVolume()).isEqualTo(8910);
    }

    @Test
    public void testTrailingGarbageIgnored() throws Exception{
        JacksonResponseReader reader = new JacksonResponseReader(createObjectMapper(), true);
        
        InvocationResult invocationResult = new InvocationResult(
                "{\"status\":\"success\",\"data\":{\"bought\":0,\"remaining\":\"1\",\"order_id\":\"372351\",\"funds\":{\"usd\":\"0.00000000\",\"eur\":\"0\",\"btc\":\"0.01010606\",\"ltc\":\"0\",\"nmc\":\"0\",\"trc\":\"0\",\"dvc\":\"0\",\"ppc\":\"0\",\"ftc\":\"0\",\"wdc\":\"0\",\"dgc\":\"0\",\"xpm\":\"0\",\"ctb\":\"0\",\"ctl\":\"0\",\"esb\":\"0\",\"esl\":\"0\",\"ggb\":\"0\",\"amb\":\"0\",\"utc\":\"0\"}}}<html><head><title>500 Internal Server Error</title></head><body><h1>Internal Server Error</h1><p><i>Failed to connect to ::1: Network is unreachable</i></p><p></p></body></html>\n",
                200);
        
        Object result = reader.read(invocationResult,
                new RestMethodMetadata(Map.class, HttpMethod.GET, null, null, null,
                        RuntimeException.class, null, MediaType.APPLICATION_JSON, null, null, null));
        
        assert(Map.class.isAssignableFrom(result.getClass()));
        Map map = (Map)result;
        
        final Object bought = ((Map) map.get("data")).get("bought");
        assertThat(bought.toString()).isEqualTo("0");
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testExceptionPropertyConflict() throws Exception {
        JacksonResponseReader reader = new JacksonResponseReader(createObjectMapper(), true);

        final Exception ex = reader.readException("{\"message\": \"msg\", \"cause\":\"cs\", \"stackTrace\":\"st\", \"backtrace\":\"bt\", \"detailMessage\":\"dm\"}", HttpStatusExceptionSupport.class);
        assertThat(ex.getMessage()).contains("msg");
        log.debug("ex = " + ex);
    }

    private ObjectMapper createObjectMapper() {
        return new DefaultJacksonObjectMapperFactory().createObjectMapper();
    }
}
