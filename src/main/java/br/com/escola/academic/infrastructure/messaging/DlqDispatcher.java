package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.adapter.out.persistence.DlqEventRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqDispatcher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DlqEventRepositoryAdapter dlqRepository;

    private static final String DLQ_TOPIC = "DLQ_TOPIC";

    public void send(String originalMessage, String reason) {

        String payload = """
            {
              "originalMessage": %s,
              "reason": "%s"
            }
            """.formatted(originalMessage, reason);

        // =========================================
        // 1. BANCO (FONTE DE VERDADE ATUAL)
        // =========================================
        dlqRepository.save(originalMessage, reason);

        // =========================================
        // 2. KAFKA DLQ
        // =========================================
        kafkaTemplate.send(DLQ_TOPIC, payload);

        log.warn("☠️ DLQ enviada com sucesso: reason={}", reason);
    }
}