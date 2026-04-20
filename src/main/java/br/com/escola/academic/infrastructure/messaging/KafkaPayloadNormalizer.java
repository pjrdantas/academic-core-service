package br.com.escola.academic.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPayloadNormalizer {

    private static final int MAX_UNWRAP_DEPTH = 3;

    private final ObjectMapper objectMapper;

    public String normalizeToJsonObject(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload inválido: vazio");
        }

        try {
            JsonNode node = objectMapper.readTree(payload);

            int depth = 0;
            while (node != null && node.isTextual() && depth < MAX_UNWRAP_DEPTH) {
                node = objectMapper.readTree(node.asText());
                depth++;
            }

            if (node == null || !node.isObject()) {
                throw new IllegalArgumentException("Payload inválido: esperado objeto JSON");
            }

            return objectMapper.writeValueAsString(node);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Payload inválido: JSON malformado", ex);
        }
    }
}
