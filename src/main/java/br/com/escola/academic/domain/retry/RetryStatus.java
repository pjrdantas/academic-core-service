package br.com.escola.academic.domain.retry;

public enum RetryStatus {

    PENDING,
    PROCESSING,
    DONE,
    CLAIMED,
    DLQ;

    public boolean isFinal() {
        return this == DONE || this == DLQ;
    }

    public boolean canRetry() {
        return this == PENDING;
    }
}