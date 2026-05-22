package com.macondo.jewelry.Integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class WompiSignatureServiceTest {
    private final WompiSignatureService signatureService = new WompiSignatureService(new WompiProperties(
            "pub_test_demo",
            "secret_integrity",
            "secret_webhook",
            "COP",
            BigDecimal.valueOf(0.032),
            70000
    ));

    @Test
    void createsWompiIntegritySignatureWithReferenceAmountCurrencyAndSecret() {
        String signature = signatureService.integritySignature("MAC-123", 450000);

        assertThat(signature).isEqualTo("76180522ee5ebc6959b4d71a82449de8f2a20c2d38947240aeb7ff370ab5fd05");
    }

    @Test
    void rejectsWebhookSignatureWhenHeaderIsMissing() {
        assertThat(signatureService.isValidWebhookSignature("{\"event\":\"transaction.updated\"}", null)).isFalse();
    }
}
