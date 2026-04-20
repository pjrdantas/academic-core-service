package br.com.escola.academic.adapter.out.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pessoa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PessoaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}