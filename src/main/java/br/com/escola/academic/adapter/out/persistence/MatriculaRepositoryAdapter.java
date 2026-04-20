package br.com.escola.academic.adapter.out.persistence;

import br.com.escola.academic.adapter.out.persistence.entity.*;
import br.com.escola.academic.adapter.out.persistence.mapper.MatriculaMapper;
import br.com.escola.academic.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.academic.application.port.out.MatriculaRepository;
import br.com.escola.academic.domain.core.Matricula;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MatriculaRepositoryAdapter implements MatriculaRepository {

    private final MatriculaJpaRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Matricula salvar(Matricula matricula) {

        MatriculaEntity entity = MatriculaMapper.toEntity(matricula);

        // garantir entidades gerenciadas
        if (entity.getAluno() != null && entity.getAluno().getId() != null) {
            entity.setAluno(entityManager.getReference(AlunoEntity.class, entity.getAluno().getId()));
        }

        if (entity.getTurma() != null && entity.getTurma().getId() != null) {
            entity.setTurma(entityManager.getReference(TurmaEntity.class, entity.getTurma().getId()));
        }

        if (entity.getPeriodoLetivo() != null && entity.getPeriodoLetivo().getId() != null) {
            entity.setPeriodoLetivo(entityManager.getReference(PeriodoLetivoEntity.class, entity.getPeriodoLetivo().getId()));
        }

        MatriculaEntity saved = repository.save(entity);

        return MatriculaMapper.toDomain(saved);
    }

    @Override
    public Optional<Matricula> buscarPorId(UUID id) {
        return repository.findById(id)
                .map(MatriculaMapper::toDomain);
    }

    @Override
    public boolean existeAlunoNaTurma(UUID alunoId, UUID turmaId) {
        return repository.existsByAluno_IdAndTurma_Id(alunoId, turmaId);
    }

    @Override
    public long contarPorTurmaId(UUID turmaId) {
        return repository.contarPorTurmaId(turmaId);
    }

    @Override
    public Optional<Matricula> buscarPorAlunoETurma(UUID alunoId, UUID turmaId) {
        return repository.findByAluno_IdAndTurma_Id(alunoId, turmaId)
                .map(MatriculaMapper::toDomain);
    }
}