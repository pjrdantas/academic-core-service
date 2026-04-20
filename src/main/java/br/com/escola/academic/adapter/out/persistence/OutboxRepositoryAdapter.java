package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.mapper.OutboxMapper;
import br.com.escola.academic.adapter.out.persistence.repository.OutboxJpaRepository;
import br.com.escola.academic.application.port.out.OutboxRepository;
import br.com.escola.academic.domain.event.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final OutboxJpaRepository repository;

    @Override
    public void save(OutboxEvent event) {
        repository.save(OutboxMapper.toEntity(event));
    }

    @Override
    public List<OutboxEvent> findPendingBatch(int limit) {
        return repository.findBatchForUpdate(limit)
                .stream()
                .map(OutboxMapper::toDomain)
                .toList();
    }

    @Override
    public void update(OutboxEvent event) {
        repository.save(OutboxMapper.toEntity(event));
    }
}