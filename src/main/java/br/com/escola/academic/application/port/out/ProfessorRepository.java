package br.com.escola.academic.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.academic.domain.core.Professor;

public interface ProfessorRepository {

    Professor salvar(Professor professor);

    Optional<Professor> buscarPorId(UUID id);
}