package br.com.escola.academic.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxEventPublisher publisher;

    @Scheduled(fixedDelay = 2000)
    public void run() {
        publisher.publish();
    }
}