package br.com.escola.academic.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.academic.domain.core.Disciplina;

public interface DisciplinaRepository {

    Disciplina salvar(Disciplina disciplina);

    Optional<Disciplina> buscarPorId(UUID id);
}