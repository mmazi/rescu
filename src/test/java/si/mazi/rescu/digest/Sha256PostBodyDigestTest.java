/**
 * Copyright (C) 2012 - 2013 Matija Mazi
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
package si.mazi.rescu.digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

public class Sha256PostBodyDigestTest {

    private static final Logger log = LoggerFactory.getLogger(Sha256PostBodyDigestTest.class);

    @Test
    public void testSignature() throws GeneralSecurityException {

        String restSign = Sha256PostBodyDigest.createInstance()
                .digestParams("{\"hello\": \"world\"}");
        
        log.debug("Rest-Sign    : " + restSign);
        String expectedResult = "X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=";
        log.debug("Expected-Sign: " + expectedResult);

        Assert.assertEquals(restSign, expectedResult);
    }
}
