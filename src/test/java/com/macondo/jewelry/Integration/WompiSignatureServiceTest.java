package com.macondo.jewelry.Integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        JsonNode event = new ObjectMapper().createObjectNode();

        assertThat(signatureService.isValidWebhookSignature(event, null)).isFalse();
    }

    @Test
    void validatesWompiEventChecksumUsingDynamicProperties() throws Exception {
        WompiSignatureService service = new WompiSignatureService(new WompiProperties(
                "pub_test_demo",
                "secret_integrity",
                "prod_events_OcHnIzeBl5socpwByQ4hA52Em3USQ93Z",
                "COP",
                BigDecimal.valueOf(0.032),
                70000
        ));
        JsonNode event = new ObjectMapper().readTree("""
                {
                  "data": {
                    "transaction": {
                      "id": "1234-1610641025-49201",
                      "status": "APPROVED",
                      "amount_in_cents": 4490000
                    }
                  },
                  "signature": {
                    "properties": [
                      "transaction.id",
                      "transaction.status",
                      "transaction.amount_in_cents"
                    ],
                    "checksum": "5A18EC5E8FDB7DF463E9F94774CBA8F583BA21BD04A09CEFF2EA68A4BC0AEFBE"
                  },
                  "timestamp": 1530291411
                }
                """);

        assertThat(service.isValidWebhookSignature(event, null)).isTrue();
    }
}
