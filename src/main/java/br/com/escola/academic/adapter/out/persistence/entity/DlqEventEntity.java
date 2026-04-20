package br.com.escola.academic.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "dlq_event", indexes = {
        @Index(name = "idx_dlq_reprocessed", columnList = "reprocessed"),
        @Index(name = "idx_dlq_created_at", columnList = "createdAt")
})
public class DlqEventEntity {

    @Id
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String reason;

    private boolean reprocessed;

    private LocalDateTime createdAt;
    
    private int retryCount;
    
    private LocalDateTime nextRetryAt;
}