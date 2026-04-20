package br.com.escola.academic.infrastructure.observability;

import org.slf4j.MDC;

public class CorrelationContext {

    public static final String EVENT_ID = "eventId";
    public static final String SAGA_ID = "sagaId";

    public static void set(String eventId, String sagaId) {
        MDC.put(EVENT_ID, eventId);
        MDC.put(SAGA_ID, sagaId);
    }

    public static void clear() {
        MDC.clear();
    }
}