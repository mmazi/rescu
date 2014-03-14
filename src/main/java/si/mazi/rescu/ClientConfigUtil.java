package si.mazi.rescu;

import si.mazi.rescu.utils.Base64;

import javax.ws.rs.HeaderParam;
import java.io.UnsupportedEncodingException;

public enum ClientConfigUtil {
    ;

    public static ClientConfig addBasicAuthCredentials(ClientConfig config, String user, String password) {
        return config.add(HeaderParam.class, "Authorization", digestForBasicAuth(user, password));
    }

    static String digestForBasicAuth(String username, String password) {
        try {
            byte[] inputBytes = (username + ":" + password).getBytes("ISO-8859-1");
            return "Basic " + Base64.encodeBytes(inputBytes);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding, fix the code.", e);
        }
    }
}
