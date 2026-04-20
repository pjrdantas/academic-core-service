package br.com.escola.academic.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import br.com.escola.academic.infrastructure.messaging.KafkaPayloadNormalizer;

@WebMvcTest(DlqController.class)
class DlqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DlqEventJpaRepository repository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private KafkaPayloadNormalizer payloadNormalizer;

    @Test
    @DisplayName("📋 GET /dlq sem filtros retorna página padrão")
    void listar_semFiltros_deveRetornarPaginaPadrao() throws Exception {
        DlqEventEntity entity = new DlqEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setPayload("{\"x\":1}");
        entity.setReason("erro-de-negocio");
        entity.setCreatedAt(LocalDateTime.of(2026, 4, 20, 12, 0));
        entity.setReprocessed(false);

        when(repository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1)
        );

        mockMvc.perform(get("/dlq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reason").value("erro-de-negocio"))
                .andExpect(jsonPath("$.content[0].reprocessed").value(false));

        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("📋 GET /dlq?reprocessed=true usa consulta filtrada")
    void listar_comFiltroReprocessed_deveUsarConsultaFiltrada() throws Exception {
        when(repository.findByReprocessed(eq(true), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)
        );

        mockMvc.perform(get("/dlq").param("reprocessed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(repository).findByReprocessed(eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("♻️ POST /dlq/{id}/reprocess republica payload no Kafka e marca como reprocessed")
    void reprocess_devePublicarNoKafkaEMarcarComoReprocessado() throws Exception {
        UUID id = UUID.randomUUID();
        String payload = """
                {"eventId":"%s","sagaId":"%s","eventType":"pagamento-aprovado"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        DlqEventEntity entity = new DlqEventEntity();
        entity.setId(id);
        entity.setPayload(payload);
        entity.setReason("erro-transitorio");
        entity.setReprocessed(false);
        entity.setCreatedAt(LocalDateTime.now());

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(payloadNormalizer.normalizeToJsonObject(payload)).thenReturn("{\"eventId\":\"normalized\"}");

        mockMvc.perform(post("/dlq/{id}/reprocess", id))
                .andExpect(status().isOk());

        // ✅ payload republicado no tópico MATRICULA
        verify(kafkaTemplate).send("MATRICULA", "{\"eventId\":\"normalized\"}");

        // ✅ marcado como reprocessed=true
        verify(repository).save(argThat(e -> e.isReprocessed() && e.getId().equals(id)));
    }

    @Test
    @DisplayName("♻️ POST /dlq/{id}/reprocess com evento já reprocessado retorna 204 (idempotente)")
    void reprocess_eventoJaReprocessado_deveRetornar204() throws Exception {
        UUID id = UUID.randomUUID();

        DlqEventEntity entity = new DlqEventEntity();
        entity.setId(id);
        entity.setPayload("{}");
        entity.setReprocessed(true); // já foi reprocessado
        entity.setCreatedAt(LocalDateTime.now());

        when(repository.findById(id)).thenReturn(Optional.of(entity));

        mockMvc.perform(post("/dlq/{id}/reprocess", id))
                .andExpect(status().isNoContent());

        // ✅ Kafka nunca chamado — idempotente
        verifyNoInteractions(kafkaTemplate);

        // ✅ save nunca chamado — nada mudou
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("♻️ POST /dlq/{id}/reprocess com ID inexistente retorna 500")
    void reprocess_idInexistente_deveRetornarErro() throws Exception {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(post("/dlq/{id}/reprocess", id))
                .andExpect(status().is5xxServerError());
    }
}
