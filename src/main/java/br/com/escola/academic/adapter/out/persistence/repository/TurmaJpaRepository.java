package br.com.escola.academic.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academic.adapter.out.persistence.entity.TurmaEntity;

public interface TurmaJpaRepository extends JpaRepository<TurmaEntity, UUID> {
}