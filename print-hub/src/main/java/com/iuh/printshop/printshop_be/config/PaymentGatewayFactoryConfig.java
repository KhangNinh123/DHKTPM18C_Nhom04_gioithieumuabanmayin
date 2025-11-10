package com.iuh.printshop.printshop_be.config;

import com.iuh.printshop.printshop_be.entity.PaymentMethod;
import com.iuh.printshop.printshop_be.service.PaymentGateway;
import com.iuh.printshop.printshop_be.service.impl.MockGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class PaymentGatewayFactoryConfig {

    @Bean
    public Map<PaymentMethod, PaymentGateway> paymentGateways(MockGateway mockGateway) {
        EnumMap<PaymentMethod, PaymentGateway> map = new EnumMap<>(PaymentMethod.class);
        map.put(PaymentMethod.VNPAY, mockGateway);
        map.put(PaymentMethod.MOMO, mockGateway);
        map.put(PaymentMethod.STRIPE, mockGateway);
        return map;
    }
}
