package br.com.escola.academic.application.port.out;

import br.com.escola.academic.adapter.out.persistence.entity.RetryEventEntity;
import br.com.escola.academic.domain.retry.RetryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RetryEventRepository {

    List<RetryEventEntity> lockNextBatch(LocalDateTime now);

    void save(RetryEventEntity event);

    void markAsDone(UUID eventId);

    void markAsDlq(UUID eventId);

    void updateStatus(UUID id, RetryStatus status);
}