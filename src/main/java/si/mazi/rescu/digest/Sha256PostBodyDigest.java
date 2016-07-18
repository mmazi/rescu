/*
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
package si.mazi.rescu.digest;

import net.iharder.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class Sha256PostBodyDigest implements BodyDigest {

    private static final String SHA_256 = "SHA-256";
    private final MessageDigest messageDigest;

    /**
     * Constructor
     *
     * @throws IllegalArgumentException if key is invalid (cannot be base-64-decoded or the decoded key is invalid).
     */
    private Sha256PostBodyDigest() throws IllegalArgumentException {

        try {
            messageDigest = MessageDigest.getInstance(SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Illegal algorithm for post body digest. Check the implementation.");
        }
    }

    public static Sha256PostBodyDigest createInstance() {

        return new Sha256PostBodyDigest();
    }

    public String digestParams(String body) {

        String bodyForDigest = body;
        if (body == null)
            bodyForDigest = "";

        messageDigest.update(bodyForDigest.getBytes());
        return Base64.encodeBytes(messageDigest.digest()).trim();
    }
}
