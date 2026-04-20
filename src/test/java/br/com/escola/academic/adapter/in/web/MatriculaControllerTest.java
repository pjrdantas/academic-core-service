package br.com.escola.academic.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.academic.application.usecase.MatricularAlunoUseCase;
import br.com.escola.academic.domain.core.Matricula;

@WebMvcTest(MatriculaController.class)
class MatriculaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatricularAlunoUseCase useCase;

    @Test
    void matricular_deveRetornar200ECorpoDaMatricula() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID periodoLetivoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        Matricula matricula = new Matricula(
                matriculaId,
                alunoId,
                turmaId,
                periodoLetivoId,
                "PENDENTE",
                LocalDate.of(2026, 4, 20)
        );

        when(useCase.executar(any(UUID.class), eq(alunoId), eq(turmaId), eq(periodoLetivoId))).thenReturn(matricula);

        String body = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(alunoId, turmaId, periodoLetivoId);

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        verify(useCase).executar(any(UUID.class), eq(alunoId), eq(turmaId), eq(periodoLetivoId));
    }

    @Test
    void matricular_comTrailingSlash_deveRetornar200() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID periodoLetivoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        Matricula matricula = new Matricula(
                matriculaId,
                alunoId,
                turmaId,
                periodoLetivoId,
                "PENDENTE",
                LocalDate.of(2026, 4, 20)
        );

        when(useCase.executar(any(UUID.class), eq(alunoId), eq(turmaId), eq(periodoLetivoId))).thenReturn(matricula);

        String body = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(alunoId, turmaId, periodoLetivoId);

        mockMvc.perform(post("/matriculas/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
}

