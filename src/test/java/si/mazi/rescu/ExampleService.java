/**
 * Copyright (C) 2013 Matija Mazi
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
 */
package si.mazi.rescu;

import si.mazi.rescu.dto.DummyAccountInfo;
import si.mazi.rescu.dto.DummyTicker;
import si.mazi.rescu.dto.GenericResult;
import si.mazi.rescu.dto.Order;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Matija Mazi
 */
@Path("api/{version}")
public interface ExampleService {

    @POST
    @Path("buy/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Order buy(@FormParam("user") String user, @FormParam("password") String password, @FormParam("amount") BigDecimal amount, @FormParam("price") BigDecimal price);

    @POST
    @Path("bitcoin_withdrawal/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Object withdrawBitcoin(@PathParam("user") String user, @FormParam("password") String password, @QueryParam("amount") BigDecimal amount, @QueryParam("address") String address);

    @GET
    @Path("{ident: [a-Z]+}_{currency}/ticker")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    DummyTicker getTicker(@PathParam("ident") String tradeableIdentifier, @PathParam("currency") String currency);

    @POST
    @FormParam("method")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    DummyTicker getInfo(Long from, Long count) throws ExampleException;

    @GET
    @Path("auth")
    Object testBasicAuth(@HeaderParam("Authorization") BasicAuthCredentials credentials, @QueryParam("param") Integer value);

    @POST
    @Path("json")
    @Consumes(MediaType.APPLICATION_JSON)
    Object testJsonBody(DummyAccountInfo ticker);

    @POST
    @Path("error")
    @Consumes(MediaType.APPLICATION_JSON)
    Object io() throws IOException;

    @GET
    @Path("generic")
    @Produces(MediaType.APPLICATION_JSON)
    GenericResult<DummyTicker[]> getGeneric();

    @GET
    @Path("string")
    @Produces(MediaType.TEXT_PLAIN)
    String getString() throws MessageException;

    @PUT
    @Path("number")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    String putNumber(int number);

    @PATCH
    @Path("number")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    String updateNumber(int number);

    @GET
    @Path("nonce")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    String getNonce(@FormParam("nonce") SynchronizedValueFactory nonce);

    @GET
    @Path("testSmallNumbers")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    String testSmallNumbersQuery(@QueryParam("value") BigDecimal value);

    @GET
    @Path("testSmallNumbers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    String testSmallNumbersJson(BigDecimal value);

    @GET
    @Path("testExceptionOnArrayMethod")
    @Produces(MediaType.APPLICATION_JSON)
    DummyTicker[] testExceptionOnArrayMethod(String param) throws ExampleException;

    @GET
    @Path("ioexception")
    @Consumes(MediaType.APPLICATION_JSON)
    Object testIOExceptionDeclared(DummyAccountInfo ticker) throws IOException;

    @GET
    @Path("ioexception")
    @Consumes(MediaType.APPLICATION_JSON)
    DummyTicker testIOExceptionDeclared() throws IOException;

    @GET
    @Path("getWithBody")
    @Consumes(MediaType.APPLICATION_JSON)
    DummyTicker testGetMethodWithBody(DummyAccountInfo ticker) throws IOException;

    @POST
    @Path("formPostCollection")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Object testFromPostCollection(@FormParam("data") List<String> data);

    @POST
    @Path("formPostCollection")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Object testFromPostCollectionAsArray(@FormParam("data[]") List<String> data);

    @POST
    @Path("dateQueryParam")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Object testDateQueryParam(@QueryParam("startDate") Date date);

    @GET
    @Path("500")
    Object test500() throws IOException;

    @GET
    @Path("invocationAwareException")
    Object invocationAwareException() throws ExampleInvocationAwareException;

    @GET
    @Path("responseHeadersAwareException")
    Object responseHeadersAwareException() throws ExampleResponseHeadersAwareException;

    @DELETE
    @Path("entity/{name}/remove")
    Object removeEntity(@PathParam("name") String name);

    @POST
    @Path(value = "future_orders_info.do")
    Object getFuturesOrders(@FormParam("order_id") String orderId, @HeaderParam("sign") ParamsDigest signer)
            throws IOException;

    @POST
    @Path("resultWithResponseHeaders")
    ExampleResponseHeadersAwareResult getResultWithResponseHeaders();

    @POST
    @Path("throwsCheckedException")
    Object throwsCheckedException() throws ExampleCheckedException;
}
