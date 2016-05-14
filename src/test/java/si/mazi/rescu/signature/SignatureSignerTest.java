/**
 * Copyright (C) 2012 - 2013 Matija Mazi
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package si.mazi.rescu.signature;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import si.mazi.rescu.HttpMethod;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

public class SignatureSignerTest {

    private static final Logger log = LoggerFactory.getLogger(SignatureSignerTest.class);

    @Test
    public void testSignature() throws GeneralSecurityException, Exception {

        InputStream inputStream = SignatureSignerTest.class.getResourceAsStream("/rsa256/private.pem");
        String pemKey = IOUtils.toString(inputStream, "UTF-8");

        SignatureInfo signatureInfo = new SignatureInfo(
                "Authorization", "rsa-sha256", "Test", "(request-target) host date content-type digest content-length", pemKey
        );

        Map<String, String> httpHeaders = new HashMap<String, String>();
        httpHeaders.put("date", "Thu, 05 Jan 2014 21:31:40 GMT");
        httpHeaders.put("content-type", "application/json");
        httpHeaders.put("digest", "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=");

        Map<String, String> signaturedHeaders = SignatureSigner.addSignature("digest", HttpMethod.POST, "http://example.com/foo?param=value&pet=dog", httpHeaders, "{\"hello\": \"world\"}", signatureInfo);

        log.debug("Rest-Sign    : " + signaturedHeaders.get(signatureInfo.getHeaderName()));
        String expectedResult = String.format("Signature keyId=\"%s\",algorithm=\"%s\",signature=\"%s\"",
                signatureInfo.getKeyId(), signatureInfo.getAlgorithm(), "Ef7MlxLXoBovhil3AlyjtBwAL9g4TN3tibLj7uuNB3CROat/9KaeQ4hW2NiJ+pZ6HQEOx9vYZAyi+7cmIkmJszJCut5kQLAwuX+Ms/mUFvpKlSo9StS2bMXDBNjOh4Auj774GFj4gwjS+3NhFeoqyr/MuN6HsEnkvn6zdgfE2i0="
                );
        log.debug("Expected-Sign: " + expectedResult);

        Assert.assertEquals(signaturedHeaders.get(signatureInfo.getHeaderName()), expectedResult);
    }
}
