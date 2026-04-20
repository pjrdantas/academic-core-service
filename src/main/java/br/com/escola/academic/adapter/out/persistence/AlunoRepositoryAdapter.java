package br.com.escola.academic.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.academic.application.port.out.AlunoRepository;
import br.com.escola.academic.domain.core.Aluno;
import br.com.escola.academic.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.academic.adapter.out.persistence.mapper.AlunoMapper;

@Component
public class AlunoRepositoryAdapter implements AlunoRepository {

    private final AlunoJpaRepository jpaRepository;

    public AlunoRepositoryAdapter(AlunoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Aluno salvar(Aluno aluno) {
        return AlunoMapper.toDomain(
                jpaRepository.save(AlunoMapper.toEntity(aluno))
        );
    }

    @Override
    public Optional<Aluno> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .map(AlunoMapper::toDomain);
    }
}