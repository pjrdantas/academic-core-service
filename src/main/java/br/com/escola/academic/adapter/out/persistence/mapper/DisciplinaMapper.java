package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.domain.core.Disciplina;
import br.com.escola.academic.adapter.out.persistence.entity.DisciplinaEntity;

public class DisciplinaMapper {

    public static DisciplinaEntity toEntity(Disciplina domain) {
        return new DisciplinaEntity(
                domain.getId(),
                domain.getNome()
        );
    }

    public static Disciplina toDomain(DisciplinaEntity entity) {
        return new Disciplina(
                entity.getId(),
                entity.getNome()
        );
    }
}