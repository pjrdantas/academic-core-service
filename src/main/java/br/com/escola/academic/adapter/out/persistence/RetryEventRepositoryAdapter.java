package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.entity.RetryEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.RetryEventJpaRepository;
import br.com.escola.academic.application.port.out.RetryEventRepository;
import br.com.escola.academic.domain.retry.RetryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RetryEventRepositoryAdapter implements RetryEventRepository {

    private final RetryEventJpaRepository jpa;

    @Override
    public List<RetryEventEntity> lockNextBatch(LocalDateTime now) {
        return jpa.findByNextRetryAtBeforeAndStatus(now, RetryStatus.PENDING);
    }

    @Override
    public void save(RetryEventEntity event) {
        jpa.save(event);
    }

    @Override
    public void markAsDone(UUID eventId) {
        jpa.updateStatus(eventId, RetryStatus.DONE);
    }

    @Override
    public void markAsDlq(UUID eventId) {
        jpa.updateStatus(eventId, RetryStatus.DLQ);
    }

    @Override
    public void updateStatus(UUID id, RetryStatus status) {
        jpa.updateStatus(id, status);
    }
}