package br.com.escola.academic.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.academic.domain.core.Aluno;

public interface AlunoRepository {

    Aluno salvar(Aluno aluno);

    Optional<Aluno> buscarPorId(UUID id);
}