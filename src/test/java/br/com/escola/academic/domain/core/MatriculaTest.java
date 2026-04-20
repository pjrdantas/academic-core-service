package br.com.escola.academic.domain.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MatriculaTest {

    @Test
    void confirmar_deveAlterarStatusQuandoMatriculaPendente() {
        Matricula matricula = novaMatricula("PENDENTE");

        matricula.confirmar();

        assertEquals("CONFIRMADA", matricula.getStatus());
    }

    @Test
    void confirmar_deveFalharQuandoMatriculaCancelada() {
        Matricula matricula = novaMatricula("CANCELADA");

        IllegalStateException exception = assertThrows(IllegalStateException.class, matricula::confirmar);

        assertEquals("Não é possível confirmar uma matrícula cancelada", exception.getMessage());
    }

    @Test
    void concluir_deveFalharQuandoStatusNaoForConfirmada() {
        Matricula matricula = novaMatricula("PENDENTE");

        IllegalStateException exception = assertThrows(IllegalStateException.class, matricula::concluir);

        assertEquals("Só é possível concluir uma matrícula confirmada", exception.getMessage());
    }

    @Test
    void cancelar_deveAlterarStatusQuandoMatriculaNaoConcluida() {
        Matricula matricula = novaMatricula("PENDENTE");

        matricula.cancelar();

        assertEquals("CANCELADA", matricula.getStatus());
    }

    private static Matricula novaMatricula(String status) {
        return new Matricula(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                LocalDate.now()
        );
    }
}

