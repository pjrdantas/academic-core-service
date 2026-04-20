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

    // ✅ Mesmo valor do DlqRetryScheduler — eventos com retryCount >= MAX_RETRIES não são reprocessados
    public static final int MAX_RETRIES = 5;

    // =========================================
    // 🔁 RETRYABLE — TransientException
    // Salva com retryCount=0 → DlqRetryScheduler vai tentar novamente
    // =========================================
    public void send(String originalMessage, String reason) {

        String payload = buildPayload(originalMessage, reason);

        dlqRepository.save(originalMessage, reason);
        kafkaTemplate.send(DLQ_TOPIC, payload);

        log.warn("🔁 DLQ retryable enviada: reason={}", reason);
    }

    // =========================================
    // ☠️ DEAD — BusinessException / Exception genérica
    // Salva com retryCount=MAX_RETRIES → DlqRetryScheduler NÃO vai reprocessar
    // =========================================
    public void sendDead(String originalMessage, String reason) {

        String payload = buildPayload(originalMessage, reason);

        dlqRepository.saveDead(originalMessage, reason, MAX_RETRIES);
        kafkaTemplate.send(DLQ_TOPIC, payload);

        log.warn("☠️ DLQ definitiva (dead) enviada: reason={}", reason);
    }

    private String buildPayload(String originalMessage, String reason) {
        return """
            {
              "originalMessage": %s,
              "reason": "%s"
            }
            """.formatted(originalMessage, reason);
    }
}