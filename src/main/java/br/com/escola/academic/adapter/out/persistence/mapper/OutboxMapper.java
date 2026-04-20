package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.adapter.out.persistence.entity.OutboxEventEntity;
import br.com.escola.academic.domain.event.OutboxEvent;

public class OutboxMapper {

    public static OutboxEventEntity toEntity(OutboxEvent event) {
        return OutboxEventEntity.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .aggregateType(event.getAggregateType())
                .aggregateId(event.getAggregateId())
                .eventType(event.getEventType())
                .payload(event.getPayload())
                .status(event.getStatus())
                .retryCount(event.getRetryCount())
                .nextRetryAt(event.getNextRetryAt())
                .createdAt(event.getCreatedAt())
                .sentAt(event.getSentAt())
                .build();
    }

    public static OutboxEvent toDomain(OutboxEventEntity entity) {
        OutboxEvent event = new OutboxEvent(
                entity.getId(),
                entity.getEventId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getNextRetryAt(),
                entity.getCreatedAt(),
                entity.getSentAt()
        );
        return event;
    }
}