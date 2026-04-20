package br.com.escola.academic.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.academic.application.port.out.MatriculaRepository;
import br.com.escola.academic.application.port.out.OutboxRepository;
import br.com.escola.academic.domain.core.Matricula;
import br.com.escola.academic.domain.event.OutboxEvent;
import br.com.escola.academic.infrastructure.messaging.MatriculaSagaOrchestrator;

@ExtendWith(MockitoExtension.class)
class MatricularAlunoUseCaseTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private MatriculaSagaOrchestrator sagaOrchestrator;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MatricularAlunoUseCase useCase;

    @Test
    void executar_deveSalvarMatriculaIniciarSagaESalvarOutbox() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID periodoLetivoId = UUID.randomUUID();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        Matricula resultado = useCase.executar(matriculaId, alunoId, turmaId, periodoLetivoId);

        assertNotNull(resultado);
        assertEquals("PENDENTE", resultado.getStatus());
        assertEquals(matriculaId, resultado.getId());

        ArgumentCaptor<Matricula> matriculaCaptor = ArgumentCaptor.forClass(Matricula.class);
        verify(matriculaRepository).salvar(matriculaCaptor.capture());
        assertEquals(matriculaId, matriculaCaptor.getValue().getId());

        verify(sagaOrchestrator).iniciarSaga(any(UUID.class), org.mockito.ArgumentMatchers.eq(matriculaId));

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(outboxCaptor.capture());

        OutboxEvent outboxEvent = outboxCaptor.getValue();
        assertEquals("MATRICULA", outboxEvent.getAggregateType());
        assertEquals("pagamento-solicitado", outboxEvent.getEventType());
        assertEquals(matriculaId, outboxEvent.getAggregateId());
        assertEquals("{\"ok\":true}", outboxEvent.getPayload());
        assertNotNull(outboxEvent.getEventId());
    }

    @Test
    void executar_deveFalharQuandoSerializacaoDoEventoFalha() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID periodoLetivoId = UUID.randomUUID();

        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("erro serializacao"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> useCase.executar(matriculaId, alunoId, turmaId, periodoLetivoId)
        );

        assertEquals("Erro ao serializar evento", exception.getMessage());
        verify(outboxRepository, never()).save(any());
    }
}

