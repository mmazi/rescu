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

import java.io.InputStream;
import java.security.GeneralSecurityException;

public class HmacSha256CipherTest {

    private static final Logger log = LoggerFactory.getLogger(HmacSha256CipherTest.class);

    @Test
    public void testEncryption() throws GeneralSecurityException, Exception {

        InputStream inputStream = HmacSha256CipherTest.class.getResourceAsStream("/hs256/key.txt");
        String key = IOUtils.toString(inputStream, "UTF-8");

        String restSign = HmacSha256Cipher.createInstance(key)
                .encrypt("date: Thu, 05 Jan 2014 21:31:40 GMT");

        log.debug("Rest-Sign    : " + restSign);
        String expectedResult = "NzU4ZDAyMGRmNzQxZmQ3NDQ0YWY0Mzk5Y2YxMjUzYzA1NGI2MWQ2OTc5NjhlYjM3NTg2Y2I1MmFiMDlkN2NkNA==";
        log.debug("Expected-Sign: " + expectedResult);

        Assert.assertEquals(restSign, expectedResult);
    }
}
