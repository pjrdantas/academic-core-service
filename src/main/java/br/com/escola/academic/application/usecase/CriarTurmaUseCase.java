package br.com.escola.academic.application.usecase;

import java.util.UUID;

import br.com.escola.academic.domain.core.Turma;

public class CriarTurmaUseCase {

    public Turma executar(UUID id,
                          String nome,
                          Integer capacidade,
                          String status) {

        return new Turma(
                id,
                nome,
                capacidade,
                status
        );
    }
}