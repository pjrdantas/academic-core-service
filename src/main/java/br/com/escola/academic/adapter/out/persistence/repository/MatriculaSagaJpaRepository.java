package br.com.escola.academic.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academic.adapter.out.persistence.entity.MatriculaSagaEntity;

public interface MatriculaSagaJpaRepository 
extends JpaRepository<MatriculaSagaEntity, UUID> {

Optional<MatriculaSagaEntity> findBySagaId(UUID sagaId);
}