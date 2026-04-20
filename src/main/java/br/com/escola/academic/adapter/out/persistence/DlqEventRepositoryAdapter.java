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
    // 🔁 RETRYABLE (TransientException)
    // retryCount=0 → DlqRetryScheduler vai reprocessar
    // =========================================
    public void save(String payload, String reason) {

        var existing = repository.findFirstByPayloadAndReprocessedFalseOrderByCreatedAtDesc(payload);

        if (existing.isPresent()) {
            DlqEventEntity entity = existing.get();
            entity.setReason(reason);
            repository.save(entity);
            return;
        }

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
    // ☠️ DEAD (BusinessException / Exception)
    // retryCount=maxRetries → DlqRetryScheduler NÃO vai reprocessar
    // =========================================
    public void saveDead(String payload, String reason, int maxRetries) {

        var existing = repository.findFirstByPayloadAndReprocessedFalseOrderByCreatedAtDesc(payload);

        if (existing.isPresent()) {
            DlqEventEntity entity = existing.get();
            entity.setReason(reason);
            entity.setRetryCount(maxRetries);
            entity.setNextRetryAt(LocalDateTime.now());
            repository.save(entity);
            return;
        }

        DlqEventEntity entity = new DlqEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setPayload(payload);
        entity.setReason(reason);
        entity.setReprocessed(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRetryCount(maxRetries); // ☠️ bloqueia retry automático
        entity.setNextRetryAt(LocalDateTime.now());

        repository.save(entity);
    }

    public boolean existsByEventId(UUID eventId) {
        return repository.existsById(eventId);
    }
}