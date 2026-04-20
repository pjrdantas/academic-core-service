package br.com.escola.academic.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.academic.application.port.out.SagaEventPublisher;
import br.com.escola.academic.domain.event.MatriculaCanceladaEvent;
import br.com.escola.academic.domain.event.MatriculaConfirmadaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SagaEventPublisherImpl implements SagaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_MATRICULA_CONFIRMADA = "matricula-confirmada";
    private static final String TOPIC_MATRICULA_CANCELADA = "matricula-cancelada";

    @Override
    public void publicarMatriculaConfirmada(UUID sagaId, UUID matriculaId) {

        var event = new MatriculaConfirmadaEvent(
                UUID.randomUUID(),
                sagaId,
                matriculaId,
                Instant.now(),
                1
        );

        kafkaTemplate.send(TOPIC_MATRICULA_CONFIRMADA, toJson(event));
    }

    @Override
    public void publicarMatriculaCancelada(UUID sagaId, UUID matriculaId) {

        var event = new MatriculaCanceladaEvent(
                UUID.randomUUID(),
                sagaId,
                matriculaId,
                Instant.now(),
                1
        );

        kafkaTemplate.send(TOPIC_MATRICULA_CANCELADA, toJson(event));
    }

    private String toJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao serializar evento da saga", ex);
        }
    }
}