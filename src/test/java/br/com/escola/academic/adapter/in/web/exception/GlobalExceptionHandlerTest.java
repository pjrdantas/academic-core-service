package br.com.escola.academic.adapter.in.web.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.academic.adapter.in.web.MatriculaController;
import br.com.escola.academic.application.usecase.MatricularAlunoUseCase;

@WebMvcTest(MatriculaController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatricularAlunoUseCase useCase;

    @Test
    void deveRetornar500QuandoMetodoNaoForSuportadoNoModoDebugLegado() throws Exception {
        mockMvc.perform(get("/matriculas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("HttpRequestMethodNotSupportedException"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deveRetornar500QuandoRecursoNaoExistirNoModoDebugLegado() throws Exception {
        mockMvc.perform(get("/rota-inexistente"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("NoResourceFoundException"))
                .andExpect(jsonPath("$.status").value(500));
    }
}
