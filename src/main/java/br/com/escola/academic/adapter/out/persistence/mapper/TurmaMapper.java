package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.domain.core.Turma;
import br.com.escola.academic.adapter.out.persistence.entity.TurmaEntity;

public class TurmaMapper {

    public static Turma toDomain(TurmaEntity entity) {
        return new Turma(
                entity.getId(),
                entity.getNome(),
                entity.getCapacidade(),
                entity.getStatus()
        );
    }

    public static TurmaEntity toEntity(Turma domain) {
        return new TurmaEntity(
                domain.getId(),
                domain.getNome(),
                domain.getCapacidade(),
                domain.getStatus()
        );
    }
}