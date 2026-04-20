package br.com.escola.academic.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessedEvent {

    private UUID id;
    private UUID eventId;
    private String eventType;
    private LocalDateTime createdAt;

    public ProcessedEvent(UUID id, UUID eventId, String eventType, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.createdAt = createdAt;
    }

    public static ProcessedEvent create(UUID eventId, String eventType) {
        return new ProcessedEvent(
                null,
                eventId,
                eventType,
                LocalDateTime.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}