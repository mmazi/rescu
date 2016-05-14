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

import si.mazi.rescu.FormUrlEncodedRequestWriter;
import si.mazi.rescu.HttpMethod;
import si.mazi.rescu.Params;
import si.mazi.rescu.RestInvocation;
import si.mazi.rescu.RestMethodMetadata;
import si.mazi.rescu.RequestWriterResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import java.io.InputStream;

public class RsaSha256CipherTest {

    private static final Logger log = LoggerFactory.getLogger(RsaSha256CipherTest.class);

    @Test
    public void testEncryption() throws GeneralSecurityException, Exception {

        InputStream inputStream = RsaSha256CipherTest.class.getResourceAsStream("/rsa256/private.pem");
        String pemKey = IOUtils.toString(inputStream, "UTF-8");

        String restSign = RsaSha256Cipher.createInstance(pemKey)
                .encrypt("date: Thu, 05 Jan 2014 21:31:40 GMT");

        log.debug("Rest-Sign    : " + restSign);
        String expectedResult = "jKyvPcxB4JbmYY4mByyBY7cZfNl4OW9HpFQlG7N4YcJPteKTu4MWCLyk+gIr0wDgqtLWf9NLpMAMimdfsH7FSWGfbMFSrsVTHNTk0rK3usrfFnti1dxsM4jl0kYJCKTGI/UWkqiaxwNiKqGcdlEDrTcUhhsFsOIo8VhddmZTZ8w=";
        log.debug("Expected-Sign: " + expectedResult);

        Assert.assertEquals(restSign, expectedResult);
    }
}
