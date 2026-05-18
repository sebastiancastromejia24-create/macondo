package com.macondo.jewelry.payment;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wompi")
public record WompiProperties(
        String publicKey,
        String integritySecret,
        String webhookSecret,
        String currency,
        BigDecimal commissionRate,
        long fixedFeeCents
) {
}
