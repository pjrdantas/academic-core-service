package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.application.port.out.ProcessedEventRepository;
import br.com.escola.academic.domain.exception.BusinessException;
import br.com.escola.academic.domain.exception.TransientException;
import br.com.escola.academic.infrastructure.observability.CorrelationContext;
import br.com.escola.academic.infrastructure.observability.ObservabilityMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 🧪 Testes de validação de produção — MatriculaConsumer
 *
 * Cobre os 5 cenários do roteiro:
 * 1. ✅ Fluxo normal
 * 2. ❌ BusinessException → DLQ definitiva (sem retry)
 * 3. 🔁 TransientException → DLQ retryable (com retry)
 * 4. 💀 Idempotência — evento já processado descartado
 * 5. 💥 Exception genérica → DLQ definitiva
 */
@ExtendWith(MockitoExtension.class)
class MatriculaConsumerTest {

    @Mock
    private MatriculaSagaOrchestrator sagaOrchestrator;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private DlqDispatcher dlqDispatcher;

    @Mock
    private ObservabilityMetrics metrics;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private MatriculaConsumer consumer;

    private ObjectMapper objectMapper;

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID SAGA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // injeta ObjectMapper manualmente pois é @InjectMocks
        consumer = new MatriculaConsumer(
                sagaOrchestrator,
                processedEventRepository,
                dlqDispatcher,
                objectMapper,
                metrics
        );
        CorrelationContext.clear();
    }

    // =========================================
    // ✅ CENÁRIO 1 — Fluxo normal
    // =========================================
    @Test
    @DisplayName("✅ Fluxo normal: evento processado → salvo em processed_event → offset commitado")
    void fluxoNormal_deveProcessarESalvarEmProcessedEvent() {

        String message = buildMessage("pagamento-aprovado");

        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

        consumer.consumir(message, ack);

        // ✅ sagaOrchestrator chamado
        verify(sagaOrchestrator).onPagamentoAprovado(SAGA_ID);

        // ✅ evento salvo no processed_event (idempotência futura)
        verify(processedEventRepository).save(EVENT_ID, "pagamento-aprovado");

        // ✅ offset commitado — sem loop infinito
        verify(ack).acknowledge();

        // ✅ métrica de sucesso
        verify(metrics).success();

        // ✅ DLQ nunca chamada
        verifyNoInteractions(dlqDispatcher);
    }

    // =========================================
    // ❌ CENÁRIO 2 — Erro de negócio → DLQ definitiva
    // =========================================
    @Test
    @DisplayName("❌ BusinessException: vai para DLQ definitiva (sem retry automático)")
    void businessException_deveIrParaDlqDefinitiva_semRetry() {

        String message = buildMessage("tipo-desconhecido"); // dispara BusinessException no switch

        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

        consumer.consumir(message, ack);

        // ☠️ sendDead() — retryCount=MAX_RETRIES → scheduler não vai reprocessar
        verify(dlqDispatcher).sendDead(eq(message), contains("Evento desconhecido"));

        // ✅ offset commitado — não fica em loop
        verify(ack).acknowledge();

        // ❌ send() retryable nunca chamado
        verify(dlqDispatcher, never()).send(anyString(), anyString());

        // ❌ processed_event nunca salvo
        verify(processedEventRepository, never()).save(any(UUID.class), anyString());
    }

    // =========================================
    // 🔁 CENÁRIO 3 — Erro transitório → DLQ retryable
    // =========================================
    @Test
    @DisplayName("🔁 TransientException: vai para DLQ retryable (DlqRetryScheduler vai tentar novamente)")
    void transientException_deveIrParaDlqRetryable() {

        String message = buildMessage("pagamento-aprovado");

        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

        // 🔁 sagaOrchestrator lança TransientException (ex: banco fora, timeout)
        doThrow(new TransientException("banco indisponível", new RuntimeException("timeout")))
                .when(sagaOrchestrator).onPagamentoAprovado(SAGA_ID);

        consumer.consumir(message, ack);

        // 🔁 send() retryable — retryCount=0 → DlqRetryScheduler vai reprocessar
        verify(dlqDispatcher).send(eq(message), contains("banco indisponível"));

        // ✅ offset commitado — retry é controlado pelo scheduler, não pelo Kafka
        verify(ack).acknowledge();

        // ☠️ sendDead() nunca chamado — ainda tem chances de retry
        verify(dlqDispatcher, never()).sendDead(anyString(), anyString());

        // ✅ métrica de retry
        verify(metrics).retry();
    }

    // =========================================
    // 💀 CENÁRIO 4 — Idempotência (evento duplicado)
    // =========================================
    @Test
    @DisplayName("💀 Idempotência: evento já processado → descartado silenciosamente")
    void eventoJaProcessado_deveDescartarSemReprocessar() {

        String message = buildMessage("pagamento-aprovado");

        // evento já existe em processed_event
        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(true);

        consumer.consumir(message, ack);

        // ✅ offset commitado — duplicata não trava o consumer
        verify(ack).acknowledge();

        // ❌ orchestrator nunca chamado — idempotência funcionando
        verifyNoInteractions(sagaOrchestrator);

        // ❌ DLQ nunca chamada
        verifyNoInteractions(dlqDispatcher);

        // ❌ processed_event.save() nunca chamado (já existe)
        verify(processedEventRepository, never()).save(any(UUID.class), anyString());
    }

    // =========================================
    // 💥 CENÁRIO 5 — Exception genérica → DLQ definitiva
    // =========================================
    @Test
    @DisplayName("💥 Exception genérica: vai para DLQ definitiva (comportamento seguro)")
    void exceptionGenerica_deveIrParaDlqDefinitiva() {

        String message = buildMessage("pagamento-aprovado");

        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

        // 💥 erro inesperado (ex: NullPointerException, erro de infra não mapeado)
        doThrow(new RuntimeException("erro inesperado no banco"))
                .when(sagaOrchestrator).onPagamentoAprovado(SAGA_ID);

        consumer.consumir(message, ack);

        // ☠️ sendDead() — erro inesperado vai para DLQ definitiva
        verify(dlqDispatcher).sendDead(eq(message), contains("erro inesperado no banco"));

        // ✅ offset commitado — evita loop infinito no Kafka
        verify(ack).acknowledge();

        // 🔁 send() retryable nunca chamado
        verify(dlqDispatcher, never()).send(anyString(), anyString());

        // ✅ métricas de erro
        verify(metrics).error();
        verify(metrics).dlq();
    }

    // =========================================
    // 🔁 CENÁRIO BÔNUS — pagamento-recusado no fluxo normal
    // =========================================
    @Test
    @DisplayName("✅ pagamento-recusado: saga falha normalmente → processed_event salvo")
    void pagamentoRecusado_deveProcessarNormalmente() {

        String message = buildMessage("pagamento-recusado");

        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

        consumer.consumir(message, ack);

        verify(sagaOrchestrator).onPagamentoRecusado(SAGA_ID);
        verify(processedEventRepository).save(EVENT_ID, "pagamento-recusado");
        verify(ack).acknowledge();
        verifyNoInteractions(dlqDispatcher);
    }

    @Test
    @DisplayName("✅ Payload duplamente serializado (DLQ retry): deve desserializar e processar normalmente")
    void payloadDuplamenteSerializado_deveProcessarNormalmente() throws Exception {

        String originalMessage = buildMessage("pagamento-aprovado");
        String quotedMessage = objectMapper.writeValueAsString(originalMessage);

        when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

        consumer.consumir(quotedMessage, ack);

        verify(sagaOrchestrator).onPagamentoAprovado(SAGA_ID);
        verify(processedEventRepository).save(EVENT_ID, "pagamento-aprovado");
        verify(ack).acknowledge();
        verifyNoInteractions(dlqDispatcher);
    }

    @Test
    @DisplayName("❌ Payload inválido sem eventId: deve ir para DLQ definitiva sem NPE")
    void payloadSemEventId_deveIrParaDlqDefinitivaSemNpe() {

        String message = """
                {
                  "sagaId": "%s",
                  "eventType": "pagamento-aprovado"
                }
                """.formatted(SAGA_ID);

        consumer.consumir(message, ack);

        verify(dlqDispatcher).sendDead(eq(message), contains("eventId"));
        verify(dlqDispatcher, never()).send(anyString(), anyString());
        verify(ack).acknowledge();
        verifyNoInteractions(sagaOrchestrator);
    }

    // =========================================
    // 🛠️ HELPER
    // =========================================
    private String buildMessage(String eventType) {
        return """
                {
                  "eventId": "%s",
                  "sagaId": "%s",
                  "eventType": "%s"
                }
                """.formatted(EVENT_ID, SAGA_ID, eventType);
    }
}

