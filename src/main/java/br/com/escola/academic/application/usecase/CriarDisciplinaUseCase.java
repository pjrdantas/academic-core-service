package br.com.escola.academic.application.usecase;

import java.util.UUID;

import br.com.escola.academic.domain.core.Disciplina;

public class CriarDisciplinaUseCase {

    public Disciplina executar(UUID id, String nome) {
        return new Disciplina(id, nome);
    }
}