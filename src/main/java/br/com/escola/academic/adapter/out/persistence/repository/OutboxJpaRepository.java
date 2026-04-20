package br.com.escola.academic.adapter.out.persistence.repository;

import br.com.escola.academic.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
        SELECT * FROM outbox_event
        WHERE status = 'PENDING'
          AND (next_retry_at IS NULL OR next_retry_at <= NOW())
        ORDER BY created_at
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
        """, nativeQuery = true)
    List<OutboxEventEntity> findBatchForUpdate(@Param("limit") int limit);
}