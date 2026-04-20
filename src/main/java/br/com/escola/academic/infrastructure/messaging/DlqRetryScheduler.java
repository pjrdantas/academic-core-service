package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.adapter.out.persistence.entity.DlqEventEntity;
import br.com.escola.academic.adapter.out.persistence.repository.DlqEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqRetryScheduler {

    private final DlqEventJpaRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaPayloadNormalizer payloadNormalizer;

    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 10000) // a cada 10s
    public void retry() {

        List<DlqEventEntity> events = repository.findRetryable(MAX_RETRIES);

        for (DlqEventEntity event : events) {

            try {

                log.info("Reprocessando DLQ {} tentativa {}", event.getId(), event.getRetryCount());

                String normalizedPayload = payloadNormalizer.normalizeToJsonObject(event.getPayload());
                kafkaTemplate.send("MATRICULA", normalizedPayload);

                event.setRetryCount(event.getRetryCount() + 1);

                // 🔥 backoff exponencial
                long delay = (long) Math.pow(2, event.getRetryCount());
                event.setNextRetryAt(LocalDateTime.now().plusSeconds(delay));

                repository.save(event);

            } catch (Exception ex) {

                log.error("Erro no retry DLQ {}", event.getId(), ex);
            }
        }
    }
}