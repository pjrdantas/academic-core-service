package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.adapter.out.persistence.entity.ProcessedEventEntity;
import br.com.escola.academic.domain.event.ProcessedEvent;

public class ProcessedEventMapper {

    public static ProcessedEventEntity toEntity(ProcessedEvent event) {
        return ProcessedEventEntity.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public static ProcessedEvent toDomain(ProcessedEventEntity entity) {
        return new ProcessedEvent(
                entity.getId(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getCreatedAt()
        );
    }
}