package com.macondo.jewelry.payment;

import com.macondo.jewelry.security.MessageDigestSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class WompiSignatureService {
    private final WompiProperties properties;

    public WompiSignatureService(WompiProperties properties) {
        this.properties = properties;
    }

    public String integritySignature(String reference, long amountInCents) {
        String payload = reference + amountInCents + properties.currency() + properties.integritySecret();
        return sha256Digest(payload);
    }

    public boolean isValidWebhookSignature(String rawBody, String receivedSignature) {
        if (receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }
        String expected = sha256(rawBody, properties.webhookSecret());
        return MessageDigestSupport.constantTimeEquals(expected, receivedSignature);
    }

    private String sha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible calcular firma Wompi", ex);
        }
    }

    private String sha256Digest(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible calcular firma de integridad", ex);
        }
    }
}
