package br.com.escola.academic.adapter.out.persistence.repository;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DlqEventJpaRepository extends JpaRepository<DlqEventEntity, UUID> {

    Page<DlqEventEntity> findByReprocessed(Boolean reprocessed, Pageable pageable);

    Page<DlqEventEntity> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<DlqEventEntity> findByReprocessedAndCreatedAtBetween(
            Boolean reprocessed,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
    
    @Query("""
    	    SELECT e FROM DlqEventEntity e
    	    WHERE e.reprocessed = false
    	      AND e.retryCount < :maxRetries
    	      AND e.nextRetryAt <= CURRENT_TIMESTAMP
    	""")
    	List<DlqEventEntity> findRetryable(int maxRetries);
}