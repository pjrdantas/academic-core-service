package br.com.escola.academic.adapter.out.persistence.repository;

import br.com.escola.academic.adapter.out.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, UUID> {

    Optional<ProcessedEventEntity> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);
}