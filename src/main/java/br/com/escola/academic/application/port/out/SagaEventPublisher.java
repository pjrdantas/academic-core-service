package br.com.escola.academic.application.port.out;

import java.util.UUID;

public interface SagaEventPublisher {

    void publicarMatriculaConfirmada(UUID sagaId, UUID matriculaId);

    void publicarMatriculaCancelada(UUID sagaId, UUID matriculaId);
}