/*
 * Copyright (C) 2019 Matija Mazi
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
import org.testng.annotations.Test;
import si.mazi.rescu.serialization.jackson.DefaultJacksonObjectMapperFactory;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;

import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class HttpRequestTest {

    @Test
    public void shouldCreateHttpRequest() throws Exception {
        ClientConfig rescuConfig = new ClientConfig();
        rescuConfig.setJacksonObjectMapperFactory(
                new DefaultJacksonObjectMapperFactory() {
                    @Override
                    public void configureObjectMapper(ObjectMapper mapper) {
                        super.configureObjectMapper(mapper);
                        mapper.configure(WRAP_EXCEPTIONS, false);
                        mapper.configure(ACCEPT_FLOAT_AS_INT, false);
                        mapper.configure(FAIL_ON_INVALID_SUBTYPE, false);
                        mapper.configure(USE_BIG_DECIMAL_FOR_FLOATS, true);
                        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                        mapper.configure(FAIL_ON_NULL_FOR_PRIMITIVES, true);
                        mapper.configure(ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
                        mapper.configure(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
                        mapper.configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
                        mapper.configure(READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
                    }
                }
        );
        TestServer testServer = RestProxyFactory.createProxy(TestServer.class, "https://en462c9ysy6rx.x.pipedream.net", rescuConfig);

        testServer.putOrder("t1", "xkcd", "89.99", "", "", "", "");
    }

    @Path("")
    interface TestServer {
        @PUT
        @Path("/order")
        @Produces(APPLICATION_JSON)
        Object putOrder(
                @QueryParam("type") String type,
                @QueryParam("account") String account,
                @QueryParam("class") String classCode,
                @QueryParam("security") String securityCode,
                @QueryParam("operation") String operationCode,
                @QueryParam("quantity") String quantity,
                @QueryParam("price") String price
        ) throws IOException;
    }
}
