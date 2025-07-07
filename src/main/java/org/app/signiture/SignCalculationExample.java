package org.app.signiture;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SignCalculationExample {
    private static final String HEX = "0123456789ABCDEF";

    public static String calculateSign(Map<String, String> requestParams, String appSecret) {
        String signSrc = buildSignSource(requestParams, appSecret);
        return md5(signSrc).toUpperCase();
    }

    private static String buildSignSource(Map<String, String> requestParams,
                                          String appSecret) {
        if (Objects.isNull(requestParams) || requestParams.isEmpty()) {
            return "";
        }
        Map<String, String> signSrcMap = requestParams;
        // Ensure that the parameters are sorted in lexicographical order
        if (!(requestParams instanceof TreeMap)) {
            signSrcMap = new TreeMap<>(requestParams);
        }
        // build sign source
        StringBuilder signSrcBuilder = new StringBuilder().append(appSecret);
        for (Map.Entry<String, String> entry : signSrcMap.entrySet()) {
            signSrcBuilder.append(entry.getKey()).append(entry.getValue());
        }
        signSrcBuilder.append(appSecret);
        return signSrcBuilder.toString();
    }

    //Convert a string to a 32-bit hexadecimal md5 value
    private static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5 = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : md5) {
                sb.append(HEX.charAt(b >> 4 & 0x0f));
                sb.append(HEX.charAt(b & 0x0f));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

