package br.com.escola.academic.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqConsumer {

    @KafkaListener(topics = "DLQ_TOPIC", groupId = "academic-dlq")
    public void consumirDlq(String message) {

        log.error("Evento recebido na DLQ: {}", message);

        // Aqui você pode:
        // ✔ salvar no banco (recomendado)
        // ✔ ou só logar (mínimo)
    }
}