package com.macondo.jewelry.Integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.macondo.jewelry.Security.MessageDigestSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
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

    public boolean isValidWebhookSignature(JsonNode event, String headerChecksum) {
        if (event == null) {
            return false;
        }
        String receivedSignature = firstPresent(headerChecksum, event.path("signature").path("checksum").asText(null));
        if (receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }
        String expected = webhookChecksum(event);
        return MessageDigestSupport.constantTimeEquals(expected.toLowerCase(), receivedSignature.toLowerCase());
    }

    private String webhookChecksum(JsonNode event) {
        JsonNode propertiesNode = event.path("signature").path("properties");
        JsonNode dataNode = event.path("data");
        if (!propertiesNode.isArray() || event.path("timestamp").isMissingNode()) {
            return "";
        }
        StringBuilder payload = new StringBuilder();
        for (JsonNode propertyNode : propertiesNode) {
            JsonNode value = findByPath(dataNode, propertyNode.asText());
            if (value.isMissingNode() || value.isNull()) {
                return "";
            }
            payload.append(value.asText());
        }
        payload.append(event.path("timestamp").asText());
        payload.append(properties.webhookSecret());
        return sha256Digest(payload.toString());
    }

    private JsonNode findByPath(JsonNode root, String path) {
        JsonNode current = root;
        for (String segment : path.split("\\.")) {
            current = current.path(segment);
            if (current.isMissingNode()) {
                return current;
            }
        }
        return current;
    }

    private String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
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
