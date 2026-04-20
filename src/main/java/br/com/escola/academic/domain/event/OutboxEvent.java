package br.com.escola.academic.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class OutboxEvent {

    private UUID id;
    private UUID eventId;
    private String aggregateType;
    private UUID aggregateId;
    private String eventType;
    private String payload;
    private OutboxStatus status;
    private int retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public OutboxEvent(UUID id,
                       UUID eventId,
                       String aggregateType,
                       UUID aggregateId,
                       String eventType,
                       String payload,
                       OutboxStatus status,
                       int retryCount,
                       LocalDateTime nextRetryAt,
                       LocalDateTime createdAt,
                       LocalDateTime sentAt) {
        this.id = id;
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public static OutboxEvent create(UUID eventId,
                                     String aggregateType,
                                     UUID aggregateId,
                                     String eventType,
                                     String payload) {
        return new OutboxEvent(
                null,
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null
        );
    }

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(int maxRetries) {
        int retry = this.retryCount + 1;

        if (retry > maxRetries) {
            this.status = OutboxStatus.DLQ;
        } else {
            this.status = OutboxStatus.PENDING;
            this.retryCount = retry;
            this.nextRetryAt = LocalDateTime.now().plusSeconds(10 * retry);
        }
    }

    // GETTERS

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }

    // SET ID (persistência)
    public void setId(UUID id) {
        this.id = id;
    }
}