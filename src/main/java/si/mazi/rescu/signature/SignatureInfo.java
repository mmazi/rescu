package si.mazi.rescu.signature;

import java.security.Signature;

/**
 * Created by igorkim on 14.05.16.
 */
public class SignatureInfo {

    private String headerName;
    private String algorithm;
    private String keyId;
    private String headerList;
    private String privateKey;

    public SignatureInfo(String headerName, String algorithm, String keyId, String headerList, String privateKey) {
        this.headerName = headerName;
        this.algorithm = algorithm;
        this.keyId = keyId;
        this.headerList = headerList;
        this.privateKey = privateKey;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getHeaderList() {
        return headerList;
    }

    public void setHeaderList(String headerList) {
        this.headerList = headerList;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
