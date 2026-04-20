package br.com.escola.academic.domain.core;

import java.util.UUID;
import lombok.Getter;

@Getter
public class Turma {

    private final UUID id;
    private final String nome;
    private final Integer capacidade;
    private final String status;

    public Turma(UUID id, String nome, Integer capacidade, String status) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        this.status = status;
    }

    public void validarCapacidade(long totalMatriculados) {
        if (totalMatriculados >= capacidade) {
            throw new IllegalStateException("Turma lotada");
        }
    }
}