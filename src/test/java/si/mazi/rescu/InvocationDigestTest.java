/*
 * Copyright (C) 2021 Matija Mazi
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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class InvocationDigestTest {

    private final RequestWriterResolver requestWriterResolver =
            RequestWriterResolver.createDefault(new ObjectMapper());

    @Test
    public void shouldDigestUrlInFormParam() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestUrlInFormParam",
                new Object[]{"v1", new UrlDigest()},
                String.class, UrlDigest.class);

        assertThat(restInvocation.getRequestBody()).contains("signature=_50_"); // https://example.com/api/digestUrlInFormParam?q1=v1
    }

    @Test
    public void shouldDigestUrlInHeader() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestUrlInHeader",
                new Object[]{"v1", new UrlDigest()},
                String.class, UrlDigest.class);

        assertThat(restInvocation.getHttpHeadersFromParams()).contains(entry("signature", "_47_")); // https://example.com/api/digestUrlInHeader?q1=v1
    }

    @Test
    public void shouldDigestBodyInHeader() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestBodyInHeader",
                new Object[]{"This method body is 39 characters long.", new BodyDigest()},
                String.class, BodyDigest.class);

        assertThat(restInvocation.getHttpHeadersFromParams()).contains(entry("signature", "_39_"));
    }

    @Test
    public void shouldDigestBodyInQueryParam() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestBodyInQueryParam",
                new Object[]{"This method body is 39 characters long.", new BodyDigest()},
                String.class, BodyDigest.class);

        assertThat(restInvocation.getInvocationUrl()).contains("signature=_39_");
    }

    @Test
    public void shouldDigestHeadersInFormParam() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestHeadersInFormParam",
                new Object[]{"first", "second", new HeaderDigest()},
                String.class, String.class, HeaderDigest.class);

        assertThat(restInvocation.getRequestBody()).contains("signature=_2_");
    }

    @Test
    public void shouldDigestHeadersInQueryParam() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestHeadersInQueryParam",
                new Object[]{"first", "second", new HeaderDigest()},
                String.class, String.class, HeaderDigest.class);

        assertThat(restInvocation.getInvocationUrl()).contains("signature=_2_");
    }

    @Test
    public void shouldDigestUrlInPlainBody() throws Exception {
        RestInvocation restInvocation = getRestInvocation(
                "digestUrlInPlainBody",
                new Object[]{"v1", new UrlDigest()},
                String.class, UrlDigest.class);

        assertThat(restInvocation.getRequestBody()).isEqualTo("_50_"); // https://example.com/api/digestUrlInPlainBody?q1=v1
    }

    private RestInvocation getRestInvocation(String methodName, Object[] invocationArguments, Class<?>... methodParamTypes) throws NoSuchMethodException {
        return RestInvocation.create(requestWriterResolver,
                RestMethodMetadata.create(
                        DigestService.class.getDeclaredMethod(methodName, methodParamTypes),
                        "https://example.com/",
                        "api"),
                invocationArguments,
                new HashMap<>()
        );
    }

    private static String wrapInUnderscores(int number) {
        return String.format("_%d_", number);
    }

    /** Digests the body by returning its length between underscores. */
    static class BodyDigest implements ParamsDigest {
        @Override public String digestParams(RestInvocation restInvocation) {
            return wrapInUnderscores(restInvocation.getRequestBody().length());
        }
    }

    /** Digests the URL by returning its length between underscores. */
    static class UrlDigest implements ParamsDigest {
        @Override public String digestParams(RestInvocation restInvocation) {
            return wrapInUnderscores(restInvocation.getInvocationUrl().length());
        }
    }

    /** Digests the headers by returning their count between underscores. */
    static class HeaderDigest implements ParamsDigest {
        @Override public String digestParams(RestInvocation restInvocation) {
            return wrapInUnderscores(restInvocation.getHttpHeadersFromParams().size());
        }
    }

    @SuppressWarnings("RestParamTypeInspection")
    @Path("api")
    public interface DigestService {

        @POST
        @Path("digestUrlInHeader")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_JSON)
        String digestUrlInHeader(
                @QueryParam("q1") String q1,
                @HeaderParam("signature") UrlDigest digest
        );

        @POST
        @Path("digestUrlInFormParam")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        String digestUrlInFormParam(
                @QueryParam("q1") String q1,
                @FormParam("signature") UrlDigest digest
        );

        @POST
        @Path("digestBodyInHeader")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        String digestBodyInHeader(
                String body,
                @HeaderParam("signature") BodyDigest digest
        );

        @POST
        @Path("digestBodyInQueryParam")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        String digestBodyInQueryParam(
                String body,
                @QueryParam("signature") BodyDigest digest
        );

        @POST
        @Path("digestHeadersInFormParam")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        String digestHeadersInFormParam(
                @HeaderParam("h1") String h1,
                @HeaderParam("h2") String h2,
                @FormParam("signature") HeaderDigest digest
        );

        @POST
        @Path("digestHeadersInQueryParam")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_JSON)
        String digestHeadersInQueryParam(
                @HeaderParam("h1") String h1,
                @HeaderParam("h2") String h2,
                @QueryParam("signature") HeaderDigest digest
        );

        @POST
        @Path("digestUrlInPlainBody")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        String digestUrlInPlainBody(
                @QueryParam("q1") String q1,
                UrlDigest digest
        );
    }
}
