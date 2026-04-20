package br.com.escola.academic.adapter.out.persistence.repository;

import br.com.escola.academic.adapter.out.persistence.entity.RetryEventEntity;
import br.com.escola.academic.domain.retry.RetryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RetryEventJpaRepository extends JpaRepository<RetryEventEntity, UUID> {

    List<RetryEventEntity> findByNextRetryAtBeforeAndStatus(
            LocalDateTime now,
            RetryStatus status
    );

    @Modifying
    @Query("""
        update RetryEventEntity r
           set r.status = :status
         where r.id = :id
    """)
    void updateStatus(UUID id, RetryStatus status);
}