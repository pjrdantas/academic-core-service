package br.com.escola.academic.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.academic.domain.core.Matricula;

public interface MatriculaRepository {

    Matricula salvar(Matricula matricula);

    Optional<Matricula> buscarPorId(UUID id);

    Optional<Matricula> buscarPorAlunoETurma(UUID alunoId, UUID turmaId);

    long contarPorTurmaId(UUID turmaId);

    boolean existeAlunoNaTurma(UUID alunoId, UUID turmaId);
}