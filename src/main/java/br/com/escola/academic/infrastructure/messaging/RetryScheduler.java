package br.com.escola.academic.infrastructure.messaging;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.escola.academic.application.port.out.RetryEventRepository;
import br.com.escola.academic.domain.retry.RetryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryScheduler {

    private final RetryEventRepository retryRepository;
    private final MatriculaSagaOrchestrator sagaOrchestrator;
    private final DlqDispatcher dlqDispatcher;

    @Scheduled(fixedDelay = 5000)
    public void processRetryQueue() {

        var now = LocalDateTime.now();

        var events = retryRepository.lockNextBatch(now);

        for (var event : events) {

            try {

                event.setStatus(RetryStatus.PROCESSING);

                sagaOrchestrator.reprocess(
                        event.getSagaId(),
                        event.getPayload()
                );

                retryRepository.markAsDone(event.getEventId());

                log.info("✅ Retry OK: {}", event.getEventId());

            } catch (Exception ex) {

                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(ex.getMessage());

                if (event.getAttempts() >= event.getMaxAttempts()) {

                    dlqDispatcher.send(event.getPayload(), ex.getMessage());

                    retryRepository.markAsDlq(event.getEventId());

                    log.error("☠️ DLQ: {}", event.getEventId());

                } else {

                    event.setStatus(RetryStatus.PENDING);

                    event.setNextRetryAt(
                            now.plusSeconds((long) Math.pow(2, event.getAttempts()))
                    );

                    retryRepository.save(event);
                }
            }
        }
    }
}