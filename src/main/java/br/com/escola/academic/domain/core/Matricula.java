package br.com.escola.academic.domain.core;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Matricula {

    private final UUID id;

    private UUID alunoId;
    private UUID turmaId;
    private UUID periodoLetivoId;

    private String status;

    private final LocalDate dataMatricula;

    public Matricula(UUID id,
                     UUID alunoId,
                     UUID turmaId,
                     UUID periodoLetivoId,
                     String status,
                     LocalDate dataMatricula) {

        this.id = id;
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.periodoLetivoId = periodoLetivoId;
        this.status = status;
        this.dataMatricula = dataMatricula;
    }

    public void atualizar(UUID alunoId, UUID turmaId, UUID periodoLetivoId) {
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.periodoLetivoId = periodoLetivoId;
    }

    public void confirmar() {
        if ("CANCELADA".equals(this.status)) {
            throw new IllegalStateException("Não é possível confirmar uma matrícula cancelada");
        }
        this.status = "CONFIRMADA";
    }

    public void cancelar() {
        if ("CONCLUIDA".equals(this.status)) {
            throw new IllegalStateException("Não é possível cancelar uma matrícula concluída");
        }
        this.status = "CANCELADA";
    }

    public void concluir() {
        if (!"CONFIRMADA".equals(this.status)) {
            throw new IllegalStateException("Só é possível concluir uma matrícula confirmada");
        }
        this.status = "CONCLUIDA";
    }
}