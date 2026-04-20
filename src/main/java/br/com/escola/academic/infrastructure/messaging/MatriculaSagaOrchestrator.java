package br.com.escola.academic.infrastructure.messaging;

import br.com.escola.academic.application.port.out.MatriculaRepository;
import br.com.escola.academic.application.port.out.MatriculaSagaRepository;
import br.com.escola.academic.domain.core.Matricula;
import br.com.escola.academic.domain.saga.MatriculaSaga;
import br.com.escola.academic.domain.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatriculaSagaOrchestrator {

    private final MatriculaSagaRepository sagaRepository;
    private final MatriculaRepository matriculaRepository;

    // =========================================
    // 🚀 INÍCIO DA SAGA (OK)
    // =========================================
    @Transactional
    public void iniciarSaga(UUID sagaId, UUID matriculaId) {

        MatriculaSaga saga = MatriculaSaga.start(sagaId, matriculaId);
        saga.markPaymentPending();

        sagaRepository.save(saga);

        log.info("🚀 Saga {} iniciada com status {}", saga.getId(), saga.getStatus());
    }

    // =========================================
    // ✔ PAGAMENTO APROVADO (IDEMPOTENTE)
    // =========================================
    @Transactional
    public void onPagamentoAprovado(UUID sagaId) {

        MatriculaSaga saga = sagaRepository.findById(sagaId);

        // 🔥 IDEMPOTÊNCIA (evita reprocessamento duplicado)
        if (saga.getStatus() == SagaStatus.COMPLETED) {
            log.warn("⚠️ Saga {} já COMPLETED (ignorado)", sagaId);
            return;
        }

        if (saga.getStatus() == SagaStatus.FAILED) {
            log.warn("⚠️ Saga {} está FAILED (ignorado)", sagaId);
            return;
        }

        saga.markPaymentConfirmed();

        Matricula matricula = matriculaRepository.buscarPorId(saga.getMatriculaId())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));

        matricula.confirmar();
        saga.complete();

        sagaRepository.update(saga);
        matriculaRepository.salvar(matricula);

        log.info("✅ Saga {} COMPLETED", saga.getId());
    }

    // =========================================
    // ❌ PAGAMENTO RECUSADO (IDEMPOTENTE)
    // =========================================
    @Transactional
    public void onPagamentoRecusado(UUID sagaId) {

        MatriculaSaga saga = sagaRepository.findById(sagaId);

        // 🔥 IDEMPOTÊNCIA
        if (saga.getStatus() == SagaStatus.FAILED) {
            log.warn("⚠️ Saga {} já FAILED (ignorado)", sagaId);
            return;
        }

        if (saga.getStatus() == SagaStatus.COMPLETED) {
            log.warn("⚠️ Saga {} já COMPLETED (ignorado)", sagaId);
            return;
        }

        Matricula matricula = matriculaRepository.buscarPorId(saga.getMatriculaId())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));

        matricula.cancelar();
        saga.fail();

        sagaRepository.update(saga);
        matriculaRepository.salvar(matricula);

        log.info("❌ Saga {} FAILED", saga.getId());
    }

    // =========================================
    // 🔁 RETRY DETERMINÍSTICO (NÍVEL PRODUÇÃO)
    // =========================================
    @Transactional
    public void reprocess(UUID sagaId, String payload) {

        log.info("🔁 Reprocessando saga {} com payload", sagaId);

        MatriculaSaga saga = sagaRepository.findById(sagaId);

        // 🔥 segurança contra reprocessamento inválido
        if (saga.getStatus() == SagaStatus.COMPLETED) {
            log.warn("⚠️ Saga {} já finalizada (ignore retry)", sagaId);
            return;
        }

        if (saga.getStatus() == SagaStatus.FAILED) {
            log.warn("⚠️ Saga {} em FAILED (retry bloqueado)", sagaId);
            return;
        }

        // ⚠️ por enquanto mantém payload como log
        log.info("📦 Payload recebido no retry: {}", payload);

        sagaRepository.update(saga);
    }
}