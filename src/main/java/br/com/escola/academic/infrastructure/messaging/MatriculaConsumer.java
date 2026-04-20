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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatriculaConsumer {

    private final MatriculaSagaOrchestrator sagaOrchestrator;
    private final ProcessedEventRepository processedEventRepository;
    private final DlqDispatcher dlqDispatcher;
    private final ObjectMapper objectMapper;
    private final ObservabilityMetrics metrics;

    @KafkaListener(
    	    topics = "MATRICULA",
    	    groupId = "academic-core-test" // 👈 novo
    	)
    @Transactional
    public void consumir(String message) {

        // 🔥 LOG CRÍTICO — se isso não aparecer, Kafka não está funcionando
        log.info("🔥🔥🔥 EVENTO RECEBIDO DO KAFKA: {}", message);

        UUID eventId = null;
        String eventType = null;

        try {

            JsonNode json = objectMapper.readTree(message);

            eventId = UUID.fromString(json.get("eventId").asText());
            UUID sagaId = UUID.fromString(json.get("sagaId").asText());
            eventType = json.get("eventType").asText();

            // ✔ correlation
            CorrelationContext.set(eventId.toString(), sagaId.toString());

            log.info("➡️ Processando evento {} tipo {}", eventId, eventType);

            // =========================================
            // 1. IDEMPOTÊNCIA
            // =========================================
            if (processedEventRepository.existsByEventId(eventId)) {
                log.warn("⚠️ Evento já processado: {}", eventId);
                return;
            }

            // =========================================
            // 2. PROCESSAMENTO
            // =========================================
            processarEvento(json);

            // =========================================
            // 3. SUCESSO
            // =========================================
            processedEventRepository.save(eventId, eventType);
            metrics.success();

            log.info("✅ Evento processado com sucesso: {}", eventId);

        } catch (BusinessException ex) {

            log.error("❌ Erro de negócio: {}", ex.getMessage());

            metrics.error();
            metrics.dlq();
            dlqDispatcher.send(message, ex.getMessage());

        } catch (TransientException ex) {

            log.warn("🔁 Erro transitório, será reprocessado");

            metrics.retry();
            throw ex;

        } catch (Exception ex) {

            log.error("💥 Erro inesperado: {}", ex.getMessage(), ex);

            metrics.error();
            metrics.dlq();
            dlqDispatcher.send(message, ex.getMessage());

        } finally {
            CorrelationContext.clear();
        }
    }

    private void processarEvento(JsonNode json) {
        String eventType = json.get("eventType").asText();
        UUID sagaId = UUID.fromString(json.get("sagaId").asText());

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