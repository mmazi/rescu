package si.mazi.rescu.signature;

import net.iharder.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public final class HmacSha256Cipher implements Cipher{

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private final Mac mac;

    private HmacSha256Cipher(String key) throws IllegalArgumentException {

        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), HMAC_SHA_256);
            mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(secretKey);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key for hmac initialization.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Illegal algorithm for post body digest. Check the implementation.");
        }
    }

    public static HmacSha256Cipher createInstance(String key) throws IllegalArgumentException {

        return new HmacSha256Cipher(key);
    }

    public String encrypt(String body) {

        mac.update(body.getBytes());
        return Base64.encodeBytes(bytesToHex(mac.doFinal()).getBytes()).trim();
    }

    private String bytesToHex(byte[] digest) {

        return String.format("%064x", new java.math.BigInteger(1, digest));
    }
}
