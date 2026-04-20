package br.com.escola.academic.domain.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatriculaConfirmadaEvent {

    private UUID eventId;
    private UUID sagaId;
    private UUID matriculaId;
    private Instant timestamp;
    private int version;
}