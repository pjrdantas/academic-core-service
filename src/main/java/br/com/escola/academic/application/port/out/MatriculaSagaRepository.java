package br.com.escola.academic.application.port.out;

import br.com.escola.academic.domain.saga.MatriculaSaga;

import java.util.UUID;

public interface MatriculaSagaRepository {

    MatriculaSaga findById(UUID sagaId);

    void save(MatriculaSaga saga);

    void update(MatriculaSaga saga);
}