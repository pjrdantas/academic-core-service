package br.com.escola.academic.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.escola.academic.application.usecase.MatricularAlunoUseCase;
import br.com.escola.academic.domain.core.Matricula;

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatricularAlunoUseCase useCase;

    public MatriculaController(MatricularAlunoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Matricula> matricular(@RequestBody MatriculaRequest request) {

        Matricula matricula = useCase.executar(
                UUID.randomUUID(),
                request.alunoId(),
                request.turmaId(),
                request.periodoLetivoId()
        );

        return ResponseEntity.ok(matricula);
    }
}