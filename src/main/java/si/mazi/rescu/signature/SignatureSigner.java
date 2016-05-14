package si.mazi.rescu.signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.HttpMethod;
import si.mazi.rescu.digest.Sha256PostBodyDigest;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.util.*;

/**
 * Created by igorkim on 14.05.16.
 */
public class SignatureSigner {

    private final static Logger log = LoggerFactory.getLogger(SignatureSigner.class);

    public static Map<String, String> addSignature(String digestHeader, HttpMethod method, String urlString, Map<String, String> httpHeaders,
                                                   String requestBody, SignatureInfo signatureInfo) {

        try {
            URI uri = new URI(urlString);

            Map<String, String> signaturedHttpHeaders = new HashMap<String, String>();
            signaturedHttpHeaders.putAll(httpHeaders);

            addDigestHeader(digestHeader, signaturedHttpHeaders, requestBody);

            if (signatureInfo == null) {
                return signaturedHttpHeaders;
            }

            if (httpHeaders.containsKey(signatureInfo.getHeaderName())) {
                log.warn("Header '" + signatureInfo.getHeaderName() + "' is overwritten");
            }

            try {
                Cipher cipher = CipherFactory.create(signatureInfo.getAlgorithm(), signatureInfo.getPrivateKey());
                String signedData = getSignedData(method, uri, httpHeaders, requestBody, signatureInfo);

                signaturedHttpHeaders.put(signatureInfo.getHeaderName(), getHeader(signatureInfo, cipher.encrypt(signedData)));
            } catch (InvalidAlgorithmException | SignatureException e) {
                log.warn(e.getMessage());
            }

            return signaturedHttpHeaders;
        } catch (URISyntaxException e) {
            log.warn(e.getMessage());
            return httpHeaders;
        }
    }

    private static void addDigestHeader(String digestHeader, Map<String, String> httpHeaders, String requestBody) {

        if (digestHeader != null) {
            if (httpHeaders.containsKey(digestHeader)) {
                log.warn(String.format("Header '%s' is overwritten", digestHeader));
            }
            httpHeaders.put(digestHeader, Sha256PostBodyDigest.createInstance().digestParams(requestBody));
        }
    }

    private static String getHeader(SignatureInfo signatureInfo, String signature) {
        return String.format("Signature keyId=\"%s\",algorithm=\"%s\",signature=\"%s\"",
                signatureInfo.getKeyId(), signatureInfo.getAlgorithm(), signature);
    }

    private static String getSignedData(HttpMethod method, URI uri,  Map<String, String> httpHeaders, String requestBody, SignatureInfo signatureInfo) {

        List<String> headerList = new ArrayList<String>();

        if (Objects.equals(signatureInfo.getHeaderList(), "All headers")) {
            headerList.add("(request-target)");
            headerList.add("host");
            headerList.addAll(httpHeaders.keySet());
        } else {
            Collections.addAll(headerList, signatureInfo.getHeaderList().split(" "));
        }

        StringBuilder signedData = new StringBuilder();
        boolean needNewLine = false;
        for (String header : headerList) {
            if (needNewLine)
                signedData.append("\n");

            switch (header) {
                case "(request-target)":
                    signedData.append("(request-target): ").append(method.name().toLowerCase()).append(" ").append(getRelativePath(uri));
                    break;
                case "host":
                    signedData.append("host: ").append(uri.getHost());
                    break;
                case "content-length":
                    signedData.append("content-length: ").append(requestBody.length());
                    break;
                default:
                    signedData.append(header).append(": ").append(httpHeaders.get(header));
                    break;
            }
            needNewLine = true;
        }

        return signedData.toString();
    }

    private static String getRelativePath(URI uri) {
        StringBuilder relativePath = new StringBuilder();
        relativePath.append(uri.getRawPath());

        if (uri.getRawQuery() != null && uri.getRawQuery().length() > 0) {
            relativePath.append("?");
            relativePath.append(uri.getRawQuery());
        }

        return relativePath.toString();
    }
}
