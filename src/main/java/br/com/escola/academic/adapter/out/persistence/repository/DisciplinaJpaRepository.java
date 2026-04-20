package br.com.escola.academic.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academic.adapter.out.persistence.entity.DisciplinaEntity;

public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaEntity, UUID> {
}