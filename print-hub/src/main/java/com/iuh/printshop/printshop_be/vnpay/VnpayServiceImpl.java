package com.iuh.printshop.printshop_be.vnpay;

import com.iuh.printshop.printshop_be.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {

    private final VnpayConfig config;

    @Override
    public String createPaymentUrl(Order order) {

        String vnp_TxnRef = order.getCode();
        BigDecimal amount = order.getTotal().multiply(BigDecimal.valueOf(100)); // VNPAY yêu cầu x100

        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TmnCode", config.getTmnCode());
        fields.put("vnp_Amount", amount.toString());
        fields.put("vnp_CurrCode", "VND");
        fields.put("vnp_TxnRef", vnp_TxnRef);
        fields.put("vnp_OrderInfo", "Thanh toan don hang: " + order.getCode());
        fields.put("vnp_OrderType", "other");
        fields.put("vnp_Locale", "vn");
        fields.put("vnp_ReturnUrl", config.getReturnUrl());
        fields.put("vnp_IpAddr", "127.0.0.1");
        fields.put("vnp_CreateDate", vnp_CreateDate);

        String queryString = VnpayUtil.buildQuery(fields);
        String hashData = VnpayUtil.buildQuery(fields);
        String secureHash = VnpayUtil.hmacSHA512(config.getHashSecret(), hashData);

        return config.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {

        String vnpSecureHash = params.remove("vnp_SecureHash");

        String signed = VnpayUtil.buildQuery(params);
        String expected = VnpayUtil.hmacSHA512(config.getHashSecret(), signed);

        return expected.equalsIgnoreCase(vnpSecureHash);
    }
}
