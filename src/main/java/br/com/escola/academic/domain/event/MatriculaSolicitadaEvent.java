package br.com.escola.academic.domain.event;

import java.util.UUID;

import lombok.Getter;

@Getter
public class MatriculaSolicitadaEvent {

    private UUID sagaId;
    private UUID matriculaId;
    private UUID alunoId;

}