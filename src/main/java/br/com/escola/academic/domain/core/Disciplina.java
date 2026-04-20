package br.com.escola.academic.domain.core;

import java.util.UUID;
import lombok.Getter;

@Getter
public class Disciplina {

    private final UUID id;
    private final String nome;
    private boolean ativa;

    public Disciplina(UUID id, String nome) {
        this.id = id;
        this.nome = nome;
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }
}