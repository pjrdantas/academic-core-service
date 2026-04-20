package br.com.escola.academic.domain.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoProcessadoEvent {

    private UUID eventId;
    private UUID sagaId;

    private UUID matriculaId;
    private boolean pagamentoAprovado;

    private String source;
    private Instant timestamp;
    private int version;

    public boolean isPagamentoAprovado() {
        return pagamentoAprovado;
    }
}