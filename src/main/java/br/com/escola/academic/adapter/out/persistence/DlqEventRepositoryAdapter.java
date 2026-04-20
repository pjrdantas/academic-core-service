package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DlqEventRepositoryAdapter {

    private final DlqEventJpaRepository repository;

    // =========================================
    // 🔥 MÉTODO ATUAL (COMPATÍVEL)
    // =========================================
    public void save(String payload, String reason) {

        DlqEventEntity entity = new DlqEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setPayload(payload);
        entity.setReason(reason);
        entity.setReprocessed(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRetryCount(0);
        entity.setNextRetryAt(LocalDateTime.now());

        repository.save(entity);
    }

    // =========================================
    // 🔥 FUTURO (NÃO USADO AINDA)
    // =========================================
    public boolean existsByEventId(UUID eventId) {
        return repository.existsById(eventId);
    }
}