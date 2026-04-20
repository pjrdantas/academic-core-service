package br.com.escola.academic.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.academic.domain.core.Turma;

public interface TurmaRepository {

    Turma salvar(Turma turma);

    Optional<Turma> buscarPorId(UUID id);
}