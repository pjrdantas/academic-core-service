package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.mapper.ProcessedEventMapper;
import br.com.escola.academic.adapter.out.persistence.repository.ProcessedEventJpaRepository;
import br.com.escola.academic.application.port.out.ProcessedEventRepository;
import br.com.escola.academic.domain.event.ProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository repository;

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public void save(UUID eventId, String eventType) {
        ProcessedEvent event = ProcessedEvent.create(eventId, eventType);
        repository.save(ProcessedEventMapper.toEntity(event));
    }
}