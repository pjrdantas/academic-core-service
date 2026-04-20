package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.domain.core.Professor;
import br.com.escola.academic.adapter.out.persistence.entity.ProfessorEntity;

public class ProfessorMapper {

    public static ProfessorEntity toEntity(Professor domain) {
        return new ProfessorEntity(
                domain.getId(),
                null // PessoaEntity será resolvida depois (infra)
        );
    }

    public static Professor toDomain(ProfessorEntity entity) {
        return new Professor(
                entity.getId(),
                entity.getPessoa().getId()
        );
    }
}