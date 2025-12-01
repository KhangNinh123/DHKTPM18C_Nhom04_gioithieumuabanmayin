package com.iuh.printshop.printshop_be.vnpay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VnpayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    public static String buildQuery(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    try {
                        result.append(URLEncoder.encode(e.getKey(), "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(e.getValue(), "UTF-8"))
                                .append("&");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

        return result.substring(0, result.length() - 1);
    }
}
