package br.com.escola.academic.domain.core;

import java.util.UUID;
import lombok.Getter;

@Getter
public class Professor {

    private final UUID id;
    private final UUID pessoaId;
    private boolean ativo;

    public Professor(UUID id, UUID pessoaId) {
        this.id = id;
        this.pessoaId = pessoaId;
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }
}