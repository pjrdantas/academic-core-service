package br.com.escola.academic.application.port.out;

import java.util.UUID;

public interface ProcessedEventRepository {

    boolean existsByEventId(UUID eventId);

    void save(UUID eventId, String eventType);
}