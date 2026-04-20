package br.com.escola.academic.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academic.adapter.out.persistence.entity.AlunoEntity;

public interface AlunoJpaRepository extends JpaRepository<AlunoEntity, UUID> {
}