package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.application.port.out.ProcessedEventRepository;
import br.com.escola.academic.domain.exception.BusinessException;
import br.com.escola.academic.domain.exception.TransientException;
import br.com.escola.academic.infrastructure.observability.CorrelationContext;
import br.com.escola.academic.infrastructure.observability.ObservabilityMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatriculaConsumer {

    private static final int MAX_UNWRAP_DEPTH = 3;

    private final MatriculaSagaOrchestrator sagaOrchestrator;
    private final ProcessedEventRepository processedEventRepository;
    private final DlqDispatcher dlqDispatcher;
    private final ObjectMapper objectMapper;
    private final ObservabilityMetrics metrics;

    @KafkaListener(
            topics = "MATRICULA",
            groupId = "academic-core-service"  // ✅ consistente com application.yaml
    )
    public void consumir(String message, Acknowledgment ack) { // ✅ Acknowledgment obrigatório com ack-mode: manual

        log.info("🔥🔥🔥 EVENTO RECEBIDO DO KAFKA: {}", message);

        UUID eventId = null;
        String eventType = null;

        try {

            JsonNode json = parseEventNode(message);

            eventId = requiredUuid(json, "eventId");
            UUID sagaId = requiredUuid(json, "sagaId");
            eventType = requiredText(json, "eventType");

            CorrelationContext.set(eventId.toString(), sagaId.toString());

            log.info("➡️ Processando evento {} tipo {}", eventId, eventType);

            // =========================================
            // 1. IDEMPOTÊNCIA
            // =========================================
            if (processedEventRepository.existsByEventId(eventId)) {
                log.warn("⚠️ Evento já processado (idempotência): {}", eventId);
                ack.acknowledge(); // ✅ commit — duplicata descartada sem reprocessar
                return;
            }

            // =========================================
            // 2. PROCESSAMENTO
            // =========================================
            processarEvento(eventType, sagaId);

            // =========================================
            // 3. SUCESSO → processed_event + commit offset
            // =========================================
            processedEventRepository.save(eventId, eventType);
            metrics.success();
            ack.acknowledge(); // ✅ commit offset — fluxo normal concluído

            log.info("✅ Evento processado com sucesso: {}", eventId);

        } catch (BusinessException ex) {

            // =========================================
            // ❌ ERRO DE NEGÓCIO → DLQ DEFINITIVA (sem retry)
            // =========================================
            log.error("❌ Erro de negócio (sem retry): {}", ex.getMessage());
            metrics.error();
            metrics.dlq();
            dlqDispatcher.sendDead(message, ex.getMessage()); // ☠️ retryCount=MAX_RETRIES
            ack.acknowledge(); // ✅ commit — não deve ser reentregue pelo Kafka

        } catch (TransientException ex) {

            // =========================================
            // 🔁 ERRO TRANSITÓRIO → DLQ RETRYABLE
            // DlqRetryScheduler vai reprocessar com backoff exponencial
            // =========================================
            log.warn("🔁 Erro transitório, agendando retry via DLQ: {}", ex.getMessage());
            metrics.retry();
            dlqDispatcher.send(message, ex.getMessage()); // retryCount=0 → scheduler retenta
            ack.acknowledge(); // ✅ commit — retry controlado pelo scheduler, não pelo Kafka

        } catch (Exception ex) {

            // =========================================
            // 💥 ERRO INESPERADO → DLQ DEFINITIVA
            // =========================================
            log.error("💥 Erro inesperado: {}", ex.getMessage(), ex);
            metrics.error();
            metrics.dlq();
            dlqDispatcher.sendDead(message, ex.getMessage()); // ☠️ retryCount=MAX_RETRIES
            ack.acknowledge(); // ✅ commit — evita loop infinito

        } finally {
            CorrelationContext.clear();
        }
    }

    private JsonNode parseEventNode(String message) throws Exception {
        JsonNode root = objectMapper.readTree(message);

        // DLQ retries can arrive as JSON string wrappers; unwrap defensively.
        int depth = 0;
        while (root != null && root.isTextual() && depth < MAX_UNWRAP_DEPTH) {
            root = objectMapper.readTree(root.asText());
            depth++;
        }

        if (root == null || !root.isObject()) {
            throw new BusinessException("Payload inválido: esperado objeto JSON");
        }

        return root;
    }

    private String requiredText(JsonNode json, String fieldName) {
        JsonNode value = json.get(fieldName);

        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new BusinessException("Payload inválido: campo obrigatório ausente " + fieldName);
        }

        return value.asText();
    }

    private UUID requiredUuid(JsonNode json, String fieldName) {
        String rawValue = requiredText(json, fieldName);

        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Payload inválido: campo " + fieldName + " não é UUID válido");
        }
    }

    private void processarEvento(String eventType, UUID sagaId) {

        switch (eventType) {

            case "pagamento-aprovado":
                sagaOrchestrator.onPagamentoAprovado(sagaId);
                break;

            case "pagamento-recusado":
                sagaOrchestrator.onPagamentoRecusado(sagaId);
                break;

            default:
                throw new BusinessException("Evento desconhecido: " + eventType);
        }
    }
}