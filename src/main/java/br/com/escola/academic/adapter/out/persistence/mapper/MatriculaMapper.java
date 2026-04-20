package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.domain.core.Matricula;
import br.com.escola.academic.adapter.out.persistence.entity.*;

public class MatriculaMapper {

    public static MatriculaEntity toEntity(Matricula domain) {

        MatriculaEntity entity = new MatriculaEntity();

        entity.setId(domain.getId());

        // ⚠️ IMPORTANTE: só referência leve, sem "new agregado completo"
        if (domain.getAlunoId() != null) {
            AlunoEntity aluno = new AlunoEntity();
            aluno.setId(domain.getAlunoId());
            entity.setAluno(aluno);
        }

        if (domain.getTurmaId() != null) {
            TurmaEntity turma = new TurmaEntity();
            turma.setId(domain.getTurmaId());
            entity.setTurma(turma);
        }

        if (domain.getPeriodoLetivoId() != null) {
            PeriodoLetivoEntity periodo = new PeriodoLetivoEntity();
            periodo.setId(domain.getPeriodoLetivoId());
            entity.setPeriodoLetivo(periodo);
        }

        entity.setStatus(domain.getStatus());
        entity.setDataMatricula(domain.getDataMatricula());

        return entity;
    }

    public static Matricula toDomain(MatriculaEntity entity) {

        return new Matricula(
                entity.getId(),
                entity.getAluno() != null ? entity.getAluno().getId() : null,
                entity.getTurma() != null ? entity.getTurma().getId() : null,
                entity.getPeriodoLetivo() != null ? entity.getPeriodoLetivo().getId() : null,
                entity.getStatus(),
                entity.getDataMatricula()
        );
    }

    public static void updateEntity(Matricula domain, MatriculaEntity entity) {

        entity.setStatus(domain.getStatus());
        entity.setDataMatricula(domain.getDataMatricula());
    }
}