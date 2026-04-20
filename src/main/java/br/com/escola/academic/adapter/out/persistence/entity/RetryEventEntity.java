package br.com.escola.academic.adapter.out.persistence.entity;


import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.academic.domain.retry.RetryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "retry_event")
@Data
public class RetryEventEntity {

    @Id
    private UUID id;

    private UUID eventId;
    private UUID sagaId;

    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    private int attempts;
    private int maxAttempts;

    private LocalDateTime nextRetryAt;

    @Enumerated(EnumType.STRING)
    private RetryStatus status;

    private String lastError;

    private LocalDateTime createdAt;
}