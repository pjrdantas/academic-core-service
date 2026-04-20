package br.com.escola.academic.adapter.out.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "matricula",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "aluno_id", "turma_id" })
    }
)
public class MatriculaEntity {

    @Id
    private UUID id; // 🔥 FIX PRINCIPAL (REMOVER GENERATED)

    @Version
    private Long version = 0L;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private AlunoEntity aluno;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private TurmaEntity turma;

    @ManyToOne
    @JoinColumn(name = "periodo_letivo_id")
    private PeriodoLetivoEntity periodoLetivo;

    private String status;

    @Column(name = "data_matricula")
    private LocalDate dataMatricula;
}