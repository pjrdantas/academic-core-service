package br.com.escola.academic.application.usecase;

import java.util.UUID;

import br.com.escola.academic.domain.core.Aluno;


public class CriarAlunoUseCase {

    public Aluno executar(UUID alunoId, UUID pessoaId) {
        return new Aluno(alunoId, pessoaId);
    }
}