package br.com.escola.academic.domain.event;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED,
    DLQ
}