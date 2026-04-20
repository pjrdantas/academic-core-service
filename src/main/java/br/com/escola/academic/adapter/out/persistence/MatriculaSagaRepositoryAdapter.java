package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.entity.MatriculaSagaEntity;
import br.com.escola.academic.adapter.out.persistence.repository.MatriculaSagaJpaRepository;
import br.com.escola.academic.application.port.out.MatriculaSagaRepository;
import br.com.escola.academic.domain.exception.TransientException;
import br.com.escola.academic.domain.saga.MatriculaSaga;
import br.com.escola.academic.domain.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MatriculaSagaRepositoryAdapter implements MatriculaSagaRepository {

    private final MatriculaSagaJpaRepository jpaRepository;

    @Override
    public MatriculaSaga findById(UUID sagaId) {
        MatriculaSagaEntity entity = jpaRepository.findById(sagaId)
                // Eventual consistency: saga may not be committed yet when event arrives.
                .orElseThrow(() -> new TransientException("Saga não encontrada", null));

        return toDomain(entity);
    }

    @Override
    public void save(MatriculaSaga saga) {
        jpaRepository.save(toEntity(saga));
    }

    @Override
    public void update(MatriculaSaga saga) {
        jpaRepository.save(toEntity(saga));
    }

    private MatriculaSaga toDomain(MatriculaSagaEntity entity) {
        return new MatriculaSaga(
                entity.getSagaId(),
                entity.getMatriculaId(),
                SagaStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private MatriculaSagaEntity toEntity(MatriculaSaga saga) {

        MatriculaSagaEntity entity = new MatriculaSagaEntity();

        entity.setSagaId(saga.getId());
        entity.setMatriculaId(saga.getMatriculaId());
        entity.setStatus(saga.getStatus().name());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
    }
}