package br.com.escola.academic.domain.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MatriculaCriadaEvent {

    private final UUID matriculaId;
    private final UUID alunoId;
    private final UUID turmaId;
    private final UUID periodoLetivoId;

    @JsonCreator
    public MatriculaCriadaEvent(
        @JsonProperty("matriculaId") UUID matriculaId,
        @JsonProperty("alunoId") UUID alunoId,
        @JsonProperty("turmaId") UUID turmaId,
        @JsonProperty("periodoLetivoId") UUID periodoLetivoId
    ) {
        this.matriculaId = matriculaId;
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.periodoLetivoId = periodoLetivoId;
    }

    public UUID getMatriculaId() { return matriculaId; }
    public UUID getAlunoId() { return alunoId; }
    public UUID getTurmaId() { return turmaId; }
    public UUID getPeriodoLetivoId() { return periodoLetivoId; }
}