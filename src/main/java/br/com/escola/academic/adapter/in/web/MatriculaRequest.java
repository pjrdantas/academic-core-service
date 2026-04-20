package br.com.escola.academic.adapter.in.web;

import java.util.UUID;

public record MatriculaRequest(
        UUID alunoId,
        UUID turmaId,
        UUID periodoLetivoId
) {}