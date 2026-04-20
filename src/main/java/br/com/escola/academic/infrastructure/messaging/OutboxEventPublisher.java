package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.application.port.out.OutboxRepository;
import br.com.escola.academic.domain.event.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void publish() {

        List<OutboxEvent> events = repository.findPendingBatch(50);

        for (OutboxEvent event : events) {

            try {

                log.info("Publicando evento {}", event.getEventId());

                kafkaTemplate.send(
                        event.getAggregateType(),
                        event.getEventId().toString(),
                        event.getPayload()
                );

                event.markAsSent();

            } catch (Exception ex) {

                log.error("Erro ao publicar evento {}", event.getEventId(), ex);

                event.markAsFailed(5);
            }

            repository.update(event);
        }
    }
}