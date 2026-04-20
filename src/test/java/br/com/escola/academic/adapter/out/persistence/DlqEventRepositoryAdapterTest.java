package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DlqEventRepositoryAdapterTest {

    @Mock
    private DlqEventJpaRepository repository;

    @InjectMocks
    private DlqEventRepositoryAdapter adapter;

    @Test
    @DisplayName("Nao reinserir retryable quando payload ja existe ativo")
    void save_deveReaproveitarRegistroAtivo() {
        String payload = "{\"eventId\":\"999\"}";
        DlqEventEntity existing = new DlqEventEntity();
        existing.setId(UUID.randomUUID());
        existing.setPayload(payload);
        existing.setReason("erro antigo");
        existing.setReprocessed(false);
        existing.setRetryCount(2);
        existing.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        existing.setNextRetryAt(LocalDateTime.now().plusSeconds(4));

        when(repository.findFirstByPayloadAndReprocessedFalseOrderByCreatedAtDesc(payload))
                .thenReturn(Optional.of(existing));

        adapter.save(payload, "erro novo");

        ArgumentCaptor<DlqEventEntity> captor = ArgumentCaptor.forClass(DlqEventEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(existing.getId());
        assertThat(captor.getValue().getRetryCount()).isEqualTo(2);
        assertThat(captor.getValue().getReason()).isEqualTo("erro novo");
    }

    @Test
    @DisplayName("Escalar para dead no mesmo registro quando payload ja existe ativo")
    void saveDead_deveAtualizarRegistroAtivo() {
        String payload = "{\"eventId\":\"999\"}";
        DlqEventEntity existing = new DlqEventEntity();
        existing.setId(UUID.randomUUID());
        existing.setPayload(payload);
        existing.setReason("erro antigo");
        existing.setReprocessed(false);
        existing.setRetryCount(1);
        existing.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        existing.setNextRetryAt(LocalDateTime.now().plusSeconds(2));

        when(repository.findFirstByPayloadAndReprocessedFalseOrderByCreatedAtDesc(payload))
                .thenReturn(Optional.of(existing));

        adapter.saveDead(payload, "fatal", 5);

        ArgumentCaptor<DlqEventEntity> captor = ArgumentCaptor.forClass(DlqEventEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(existing.getId());
        assertThat(captor.getValue().getRetryCount()).isEqualTo(5);
        assertThat(captor.getValue().getReason()).isEqualTo("fatal");
    }
}

