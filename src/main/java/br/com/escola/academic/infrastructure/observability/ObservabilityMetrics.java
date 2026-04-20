package br.com.escola.academic.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ObservabilityMetrics {

    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter retryCounter;
    private final Counter dlqCounter;

    public ObservabilityMetrics(MeterRegistry registry) {
        this.successCounter = registry.counter("events.success");
        this.errorCounter = registry.counter("events.error");
        this.retryCounter = registry.counter("events.retry");
        this.dlqCounter = registry.counter("events.dlq");
    }

    public void success() { successCounter.increment(); }
    public void error() { errorCounter.increment(); }
    public void retry() { retryCounter.increment(); }
    public void dlq() { dlqCounter.increment(); }
}