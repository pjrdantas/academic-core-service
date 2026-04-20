package br.com.escola.academic.application.usecase;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.academic.application.port.out.MatriculaRepository;
import br.com.escola.academic.application.port.out.OutboxRepository;
import br.com.escola.academic.domain.core.Matricula;
import br.com.escola.academic.domain.event.OutboxEvent;
import br.com.escola.academic.domain.event.PagamentoSolicitadoEvent;
import br.com.escola.academic.infrastructure.messaging.MatriculaSagaOrchestrator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatricularAlunoUseCase {

    private final MatriculaRepository matriculaRepository;
    private final MatriculaSagaOrchestrator sagaOrchestrator;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Matricula executar(UUID matriculaId,
                              UUID alunoId,
                              UUID turmaId,
                              UUID periodoLetivoId) {

        // =========================================
        // 1. CRIA MATRÍCULA
        // =========================================
        Matricula matricula = new Matricula(
                matriculaId,
                alunoId,
                turmaId,
                periodoLetivoId,
                "PENDENTE",
                java.time.LocalDate.now()
        );

        matriculaRepository.salvar(matricula);

        // =========================================
        // 2. INICIA SAGA
        // =========================================
        UUID sagaId = UUID.randomUUID();
        sagaOrchestrator.iniciarSaga(sagaId, matriculaId);

        // =========================================
        // 3. CRIA EVENTO DE PAGAMENTO
        // =========================================
        PagamentoSolicitadoEvent event = new PagamentoSolicitadoEvent(
                UUID.randomUUID(), // 🔥 ESTE É O eventId GLOBAL
                sagaId,
                matriculaId,
                100.0,
                "academic-service",
                Instant.now(),
                1
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento", e);
        }

        // =========================================
        // 4. OUTBOX (NOVO MODELO CORRETO)
        // =========================================
        OutboxEvent outboxEvent = OutboxEvent.create(
                event.getEventId(),          // 🔥 MESMO ID DO EVENTO (CRÍTICO)
                "MATRICULA",                 // aggregateType = tópico
                matriculaId,                 // aggregateId
                "pagamento-solicitado",      // tipo do evento
                payload
        );

        // =========================================
        // 5. SALVA OUTBOX
        // =========================================
        outboxRepository.save(outboxEvent);

        return matricula;
    }
}