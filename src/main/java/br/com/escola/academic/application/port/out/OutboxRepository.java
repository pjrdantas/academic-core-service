package br.com.escola.academic.application.port.out;

import br.com.escola.academic.domain.event.OutboxEvent;

import java.util.List;

public interface OutboxRepository {

    void save(OutboxEvent event);

    List<OutboxEvent> findPendingBatch(int limit);

    void update(OutboxEvent event);
}