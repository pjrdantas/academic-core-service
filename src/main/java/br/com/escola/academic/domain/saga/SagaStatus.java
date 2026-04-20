package br.com.escola.academic.domain.saga;

public enum SagaStatus {
    STARTED,
    PAYMENT_PENDING,
    PAYMENT_CONFIRMED,
    COMPLETED,
    FAILED
}