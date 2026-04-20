package br.com.escola.academic.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.academic.adapter.out.persistence.entity.MatriculaEntity;
import jakarta.persistence.LockModeType;

public interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(m) FROM MatriculaEntity m WHERE m.turma.id = :turmaId")
    long contarPorTurmaId(@Param("turmaId") UUID turmaId);

    boolean existsByAluno_IdAndTurma_Id(UUID alunoId, UUID turmaId);

    Optional<MatriculaEntity> findByAluno_IdAndTurma_Id(UUID alunoId, UUID turmaId);
}