package si.mazi.rescu.signature;

import java.util.Objects;

/**
 * Created by igorkim on 14.05.16.
 */
public class CipherFactory {

    public static Cipher create(String algorithm, String privateKey) throws InvalidAlgorithmException {
        if (Objects.equals(algorithm, "rsa-sha256")) {
            return RsaSha256Cipher.createInstance(privateKey);
        }
        if (Objects.equals(algorithm, "hmac-sha256")) {
            return RsaSha256Cipher.createInstance(privateKey);
        }

        throw new InvalidAlgorithmException("Invalid signature algorithm");
    }
}
