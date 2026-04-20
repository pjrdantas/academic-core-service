package br.com.escola.academic.domain.saga;

import java.time.LocalDateTime;
import java.util.UUID;

public class MatriculaSaga {

    private UUID id;
    private UUID matriculaId;
    private SagaStatus status;
    public MatriculaSaga(UUID id,
                         UUID matriculaId,
                         SagaStatus status,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.matriculaId = matriculaId;
        this.status = status;
    }

    public static MatriculaSaga start(UUID sagaId, UUID matriculaId) {
        return new MatriculaSaga(
                sagaId,
                matriculaId,
                SagaStatus.STARTED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public void markPaymentPending() {
        this.status = SagaStatus.PAYMENT_PENDING;
        LocalDateTime.now();
    }

    public void markPaymentConfirmed() {
        this.status = SagaStatus.PAYMENT_CONFIRMED;
        LocalDateTime.now();
    }

    public void complete() {
        this.status = SagaStatus.COMPLETED;
        LocalDateTime.now();
    }

    public void fail() {
        this.status = SagaStatus.FAILED;
        LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getMatriculaId() { return matriculaId; }
    public SagaStatus getStatus() { return status; }
}