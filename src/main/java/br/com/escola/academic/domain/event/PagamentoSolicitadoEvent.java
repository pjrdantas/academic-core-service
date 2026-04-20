package br.com.escola.academic.domain.event;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PagamentoSolicitadoEvent {

    // 🔥 NOVO (idempotência)
    private UUID eventId;

    private UUID sagaId;
    private UUID matriculaId;

    // 🔥 NOVOS CAMPOS (produção)
    private Double valor;
    private String source;
    private Instant timestamp;
    private int version;

    // 🔵 CONSTRUTOR ANTIGO (mantido - compatibilidade)
    public PagamentoSolicitadoEvent(UUID sagaId, UUID matriculaId) {
        this.sagaId = sagaId;
        this.matriculaId = matriculaId;

        // valores padrão
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.source = "academic-service";
        this.version = 1;
        this.valor = 0.0;
    }

    // 🔵 CONSTRUTOR NOVO (produção)
    public PagamentoSolicitadoEvent(UUID eventId,
                                    UUID sagaId,
                                    UUID matriculaId,
                                    Double valor,
                                    String source,
                                    Instant timestamp,
                                    int version) {

        this.eventId = eventId;
        this.sagaId = sagaId;
        this.matriculaId = matriculaId;
        this.valor = valor;
        this.source = source;
        this.timestamp = timestamp;
        this.version = version;
    }
}