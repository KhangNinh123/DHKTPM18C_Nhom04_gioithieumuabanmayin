package com.iuh.printshop.printshop_be.vnpay;

import com.iuh.printshop.printshop_be.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {

    private final VnpayConfig config;

    @Override
    public String createPaymentUrl(Order order) {
        try {
            // VNPay yêu cầu amount = tiền * 100
            long amount = order.getTotal().longValue() * 100;

            String orderInfo = "Thanh toan don hang: " + order.getCode();
            String txnRef = order.getCode();
            String ipAddr = "127.0.0.1";

            // Dùng TreeMap để tự sort A-Z theo key
            Map<String, String> params = new TreeMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", config.getTmnCode());
            params.put("vnp_Amount", String.valueOf(amount));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", txnRef);
            params.put("vnp_OrderInfo", orderInfo);
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", config.getReturnUrl());
            params.put("vnp_IpAddr", ipAddr);

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String createDate = df.format(new Date());
            params.put("vnp_CreateDate", createDate);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 10);
            String expireDate = df.format(cal.getTime());
            params.put("vnp_ExpireDate", expireDate);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            // *** RẤT QUAN TRỌNG ***
            // VNPay yêu cầu: hash trên CHUỖI ĐÃ URL-ENCODE
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value != null && !value.isEmpty()) {
                    String encodedKey = URLEncoder.encode(
                            key, StandardCharsets.US_ASCII.toString());
                    String encodedValue = URLEncoder.encode(
                            value, StandardCharsets.US_ASCII.toString());

                    if (hashData.length() > 0) {
                        hashData.append('&');
                        query.append('&');
                    }

                    hashData.append(encodedKey).append('=').append(encodedValue);
                    query.append(encodedKey).append('=').append(encodedValue);
                }
            }

            String secureHash = VnpayUtil.hmacSHA512(
                    config.getHashSecret(), hashData.toString());

            // Gắn thêm vnp_SecureHash vào cuối URL
            return config.getPayUrl()
                    + "?" + query
                    + "&vnp_SecureHash=" + secureHash;

        } catch (Exception e) {
            throw new RuntimeException("Error generating VNPay URL", e);
        }
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        // Lúc VNPay gọi callback về hệ thống mình
        String vnp_SecureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder hashData = new StringBuilder();

        sorted.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                if (hashData.length() > 0) hashData.append('&');
                hashData.append(key).append('=').append(value);
            }
        });

        String checkHash = VnpayUtil.hmacSHA512(
                config.getHashSecret(), hashData.toString());

        return checkHash.equalsIgnoreCase(vnp_SecureHash);
    }
}
