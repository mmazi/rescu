/**
 * Copyright (C) 2013 Matija Mazi
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

import net.iharder.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;


public final class RsaSha256Cipher implements Cipher {

    private static final String RSA_SHA_256 = "SHA256withRSA";
    private Signature rsaSha256Signature;

    private RsaSha256Cipher(String privateKeyPem) throws IllegalArgumentException {

        try {
            Security.addProvider(new BouncyCastleProvider());

            rsaSha256Signature = Signature.getInstance(RSA_SHA_256);
            rsaSha256Signature.initSign(getPrivateKey(privateKeyPem));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not decode Base 64 string", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Illegal algorithm for post body digest. Check the implementation.");
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException("Invalid key for RSA initialization.");
        }
    }

    public static RsaSha256Cipher createInstance(String privateKeyPem) {

        return new RsaSha256Cipher(privateKeyPem);
    }

    public String encrypt(String message) throws SignatureException {

        rsaSha256Signature.update(message.getBytes());
        return Base64.encodeBytes(rsaSha256Signature.sign());
    }

    private PrivateKey getPrivateKey(String privateKeyPem) throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {

        PEMParser parser = new PEMParser(new StringReader(privateKeyPem));
        PEMKeyPair pemKeyPair = (PEMKeyPair) parser.readObject();
        KeyPair keyPair = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
        parser.close();

        return keyPair.getPrivate();
    }
}
