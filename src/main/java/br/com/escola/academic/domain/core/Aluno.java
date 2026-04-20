package br.com.escola.academic.domain.core;

import java.util.UUID;
import lombok.Getter;

@Getter
public class Aluno {

    private final UUID id;
    private final UUID pessoaId;
    private boolean ativo;

    public Aluno(UUID id, UUID pessoaId) {
        this.id = id;
        this.pessoaId = pessoaId;
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }
}