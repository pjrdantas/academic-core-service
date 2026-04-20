package br.com.escola.academic.adapter.out.persistence.mapper;

import br.com.escola.academic.domain.core.Aluno;
import br.com.escola.academic.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.academic.adapter.out.persistence.entity.PessoaEntity;

public class AlunoMapper {

    public static AlunoEntity toEntity(Aluno domain) {

        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setId(domain.getPessoaId());

        return new AlunoEntity(
                domain.getId(),
                pessoa
        );
    }

    public static Aluno toDomain(AlunoEntity entity) {

        return new Aluno(
                entity.getId(),
                entity.getPessoa().getId()
        );
    }
}