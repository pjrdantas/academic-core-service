package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 🧪 Testes do DlqRetryScheduler
 *
 * Valida:
 * - Evento retryable é republica no tópico MATRICULA
 * - retryCount é incrementado corretamente
 * - nextRetryAt aplica backoff exponencial
 * - Evento com retryCount >= MAX_RETRIES NÃO aparece (filtrado pela query)
 * - Erro no Kafka não propaga (outros eventos continuam sendo processados)
 */
@ExtendWith(MockitoExtension.class)
class DlqRetrySchedulerTest {

    @Mock
    private DlqEventJpaRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private KafkaPayloadNormalizer payloadNormalizer;

    @InjectMocks
    private DlqRetryScheduler scheduler;

    private static final int MAX_RETRIES = 5;

    // =========================================
    // 🔁 Evento retryable → re-publicado no MATRICULA
    // =========================================
    @Test
    @DisplayName("🔁 Evento retryable: é re-publicado no tópico MATRICULA")
    void retry_eventoRetryable_devePublicarNoKafka() {

        DlqEventEntity event = buildEvent(0, LocalDateTime.now().minusMinutes(1));
        when(repository.findRetryable(MAX_RETRIES)).thenReturn(List.of(event));
        when(payloadNormalizer.normalizeToJsonObject(event.getPayload())).thenReturn("{\"eventId\":\"ok\"}");

        scheduler.retry();

        verify(kafkaTemplate).send("MATRICULA", "{\"eventId\":\"ok\"}");
        verify(repository).save(event);
    }

    // =========================================
    // 📈 retryCount incrementado após retry
    // =========================================
    @Test
    @DisplayName("📈 retryCount é incrementado após cada retry")
    void retry_deveIncrementarRetryCount() {

        DlqEventEntity event = buildEvent(2, LocalDateTime.now().minusMinutes(1));
        when(repository.findRetryable(MAX_RETRIES)).thenReturn(List.of(event));
        when(payloadNormalizer.normalizeToJsonObject(event.getPayload())).thenReturn(event.getPayload());

        scheduler.retry();

        ArgumentCaptor<DlqEventEntity> captor = ArgumentCaptor.forClass(DlqEventEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getRetryCount()).isEqualTo(3); // 2 + 1
    }

    // =========================================
    // ⏰ Backoff exponencial aplicado corretamente
    // =========================================
    @Test
    @DisplayName("⏰ nextRetryAt aplica backoff exponencial (2^retryCount segundos)")
    void retry_deveAplicarBackoffExponencial() {

        DlqEventEntity event = buildEvent(3, LocalDateTime.now().minusMinutes(1));
        when(repository.findRetryable(MAX_RETRIES)).thenReturn(List.of(event));
        when(payloadNormalizer.normalizeToJsonObject(event.getPayload())).thenReturn(event.getPayload());

        LocalDateTime antes = LocalDateTime.now();
        scheduler.retry();
        LocalDateTime depois = LocalDateTime.now();

        ArgumentCaptor<DlqEventEntity> captor = ArgumentCaptor.forClass(DlqEventEntity.class);
        verify(repository).save(captor.capture());

        // Após retryCount=3 → nextRetryAt = agora + 2^4 = agora + 16 segundos
        // (após incremento, retryCount=4, delay=2^4=16)
        LocalDateTime nextRetry = captor.getValue().getNextRetryAt();
        assertThat(nextRetry).isAfter(antes.plusSeconds(14));
        assertThat(nextRetry).isBefore(depois.plusSeconds(18));
    }

    // =========================================
    // 💀 Sem eventos retryable → nenhuma ação
    // =========================================
    @Test
    @DisplayName("💀 Sem eventos retryable: nenhuma ação")
    void retry_semEventos_naoFazNada() {

        when(repository.findRetryable(MAX_RETRIES)).thenReturn(List.of());

        scheduler.retry();

        verifyNoInteractions(kafkaTemplate);
        verify(repository, never()).save(any(DlqEventEntity.class));
    }

    // =========================================
    // 🛡️ Erro no Kafka não impede outros eventos
    // =========================================
    @Test
    @DisplayName("🛡️ Erro no Kafka em um evento não impede reprocessamento dos demais")
    void retry_erroEmUmEvento_continuaComOsOutros() {

        DlqEventEntity evento1 = buildEvent(0, LocalDateTime.now().minusMinutes(1));
        DlqEventEntity evento2 = buildEvent(1, LocalDateTime.now().minusMinutes(1));

        when(repository.findRetryable(MAX_RETRIES)).thenReturn(List.of(evento1, evento2));
        when(payloadNormalizer.normalizeToJsonObject(evento1.getPayload())).thenReturn("{\"a\":1}");
        when(payloadNormalizer.normalizeToJsonObject(evento2.getPayload())).thenReturn("{\"b\":2}");
        when(kafkaTemplate.send(eq("MATRICULA"), eq("{\"a\":1}")))
                .thenThrow(new RuntimeException("Kafka fora do ar"));

        // não deve lançar exception para o scheduler
        scheduler.retry();

        // evento2 ainda é processado
        verify(kafkaTemplate).send("MATRICULA", "{\"b\":2}");
        verify(repository).save(evento2);
    }

    // =========================================
    // 🔍 Eventos com retryCount >= MAX_RETRIES não são buscados
    // (validação da query — comportamento de integração)
    // =========================================
    @Test
    @DisplayName("🔍 Query findRetryable é chamada com MAX_RETRIES correto")
    void retry_deveUsarMaxRetriesCorreto() {

        when(repository.findRetryable(MAX_RETRIES)).thenReturn(List.of());

        scheduler.retry();

        verify(repository).findRetryable(MAX_RETRIES);
    }

    // =========================================
    // 🛠️ HELPER
    // =========================================
    private DlqEventEntity buildEvent(int retryCount, LocalDateTime nextRetryAt) {
        DlqEventEntity entity = new DlqEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setPayload("""
                {
                  "eventId": "%s",
                  "sagaId": "%s",
                  "eventType": "pagamento-aprovado"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID()));
        entity.setReason("erro-transitorio");
        entity.setReprocessed(false);
        entity.setCreatedAt(LocalDateTime.now().minusHours(1));
        entity.setRetryCount(retryCount);
        entity.setNextRetryAt(nextRetryAt);
        return entity;
    }
}

