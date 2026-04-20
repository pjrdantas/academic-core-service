package br.com.escola.academic.application.usecase;

import java.util.UUID;

import br.com.escola.academic.domain.core.Professor;


public class CriarProfessorUseCase {

    public Professor executar(UUID professorId, UUID pessoaId) {
        return new Professor(professorId, pessoaId);
    }
}