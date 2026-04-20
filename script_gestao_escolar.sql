-- =========================
-- EXTENSÃO
-- =========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================
-- SCHEMA (já existe, mas mantido por segurança)
-- =========================
ALTER SCHEMA public OWNER TO postgres;

-- =========================
-- TABELAS
-- =========================

CREATE TABLE public.pessoa (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    cpf character varying(255) NOT NULL,
    data_nascimento date,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pessoa_pkey PRIMARY KEY (id),
    CONSTRAINT pessoa_cpf_key UNIQUE (cpf)
);

CREATE TABLE public.papel (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    nome character varying(50) NOT NULL,
    CONSTRAINT papel_pkey PRIMARY KEY (id),
    CONSTRAINT papel_nome_key UNIQUE (nome)
);

CREATE TABLE public.pessoa_papel (
    pessoa_id uuid NOT NULL,
    papel_id uuid NOT NULL,
    CONSTRAINT pessoa_papel_pkey PRIMARY KEY (pessoa_id, papel_id),
    CONSTRAINT pessoa_papel_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES public.pessoa(id),
    CONSTRAINT pessoa_papel_papel_id_fkey FOREIGN KEY (papel_id) REFERENCES public.papel(id)
);

CREATE TABLE public.aluno (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    pessoa_id uuid NOT NULL,
    CONSTRAINT aluno_pkey PRIMARY KEY (id),
    CONSTRAINT aluno_pessoa_id_key UNIQUE (pessoa_id),
    CONSTRAINT aluno_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES public.pessoa(id)
);

CREATE TABLE public.professor (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    pessoa_id uuid NOT NULL,
    CONSTRAINT professor_pkey PRIMARY KEY (id),
    CONSTRAINT professor_pessoa_id_key UNIQUE (pessoa_id),
    CONSTRAINT professor_pessoa_id_fkey FOREIGN KEY (pessoa_id) REFERENCES public.pessoa(id)
);

CREATE TABLE public.turma (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    capacidade integer,
    status character varying(255) DEFAULT 'ATIVA',
    CONSTRAINT turma_pkey PRIMARY KEY (id)
);

CREATE TABLE public.disciplina (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    CONSTRAINT disciplina_pkey PRIMARY KEY (id)
);

CREATE TABLE public.periodo_letivo (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    ano integer NOT NULL,
    semestre integer,
    data_inicio date,
    data_fim date,
    CONSTRAINT periodo_letivo_pkey PRIMARY KEY (id)
);

CREATE TABLE public.matricula (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    aluno_id uuid NOT NULL,
    turma_id uuid NOT NULL,
    periodo_letivo_id uuid NOT NULL,
    status character varying(255) DEFAULT 'ATIVA',
    data_matricula date,
    version bigint,
    CONSTRAINT matricula_pkey PRIMARY KEY (id),
    CONSTRAINT matricula_aluno_id_fkey FOREIGN KEY (aluno_id) REFERENCES public.aluno(id),
    CONSTRAINT matricula_turma_id_fkey FOREIGN KEY (turma_id) REFERENCES public.turma(id),
    CONSTRAINT matricula_periodo_letivo_id_fkey FOREIGN KEY (periodo_letivo_id) REFERENCES public.periodo_letivo(id),
    CONSTRAINT matricula_aluno_id_turma_id_key UNIQUE (aluno_id, turma_id),
    CONSTRAINT matricula_aluno_id_periodo_letivo_id_key UNIQUE (aluno_id, periodo_letivo_id)
);

CREATE TABLE public.oferta_disciplina (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    turma_id uuid NOT NULL,
    disciplina_id uuid NOT NULL,
    CONSTRAINT oferta_disciplina_pkey PRIMARY KEY (id),
    CONSTRAINT oferta_disciplina_turma_id_fkey FOREIGN KEY (turma_id) REFERENCES public.turma(id),
    CONSTRAINT oferta_disciplina_disciplina_id_fkey FOREIGN KEY (disciplina_id) REFERENCES public.disciplina(id)
);

CREATE TABLE public.sala (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    nome character varying(50),
    capacidade integer,
    CONSTRAINT sala_pkey PRIMARY KEY (id)
);

CREATE TABLE public.grade_horaria (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    turma_id uuid NOT NULL,
    periodo_letivo_id uuid NOT NULL,
    versao integer DEFAULT 1,
    ativa boolean DEFAULT true,
    CONSTRAINT grade_horaria_pkey PRIMARY KEY (id),
    CONSTRAINT grade_horaria_turma_id_fkey FOREIGN KEY (turma_id) REFERENCES public.turma(id),
    CONSTRAINT grade_horaria_periodo_letivo_id_fkey FOREIGN KEY (periodo_letivo_id) REFERENCES public.periodo_letivo(id)
);

CREATE TABLE public.horario_aula (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    grade_horaria_id uuid NOT NULL,
    oferta_disciplina_id uuid NOT NULL,
    professor_id uuid NOT NULL,
    sala_id uuid,
    dia_semana integer NOT NULL,
    hora_inicio time NOT NULL,
    hora_fim time NOT NULL,
    CONSTRAINT horario_aula_pkey PRIMARY KEY (id),
    CONSTRAINT horario_aula_grade_horaria_id_fkey FOREIGN KEY (grade_horaria_id) REFERENCES public.grade_horaria(id),
    CONSTRAINT horario_aula_oferta_disciplina_id_fkey FOREIGN KEY (oferta_disciplina_id) REFERENCES public.oferta_disciplina(id),
    CONSTRAINT horario_aula_professor_id_fkey FOREIGN KEY (professor_id) REFERENCES public.professor(id),
    CONSTRAINT horario_aula_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.sala(id)
);

CREATE TABLE public.matricula_saga (
    saga_id uuid NOT NULL,
    matricula_id uuid NOT NULL,
    pagamento_confirmado boolean,
    finalizado boolean DEFAULT false,
    status character varying(255) NOT NULL,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    version integer DEFAULT 0,
    CONSTRAINT matricula_saga_pkey PRIMARY KEY (saga_id),
    CONSTRAINT matricula_saga_matricula_id_key UNIQUE (matricula_id)
);

CREATE TABLE public.outbox_event (
    id uuid NOT NULL,
    aggregate_type character varying(255),
    aggregate_id uuid,
    type character varying(255),
    payload text,
    created_at timestamp,
    correlation_id character varying(255),
    retry_count integer,
    sent_at timestamp,
    next_retry_at timestamptz,
    dead_letter boolean,
    processed boolean,
    event_id uuid NOT NULL,
    event_type character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT outbox_event_pkey PRIMARY KEY (id),
    CONSTRAINT uk_outbox_event_id UNIQUE (event_id)
);

CREATE TABLE public.inbox_event (
    id uuid NOT NULL,
    event_id character varying(255) NOT NULL,
    aggregate_id character varying(255),
    event_type character varying(255),
    payload text NOT NULL,
    processed_at timestamp,
    created_at timestamp DEFAULT now() NOT NULL,
    last_error character varying(255),
    retry_count integer,
    received_at timestamp,
    status character varying(255),
    CONSTRAINT inbox_event_pkey PRIMARY KEY (id),
    CONSTRAINT inbox_event_event_id_key UNIQUE (event_id)
);

CREATE TABLE public.processed_event (
    id uuid NOT NULL,
    created_at timestamp,
    event_id uuid,
    event_type character varying(255),
    processed_at timestamp DEFAULT now(),
    CONSTRAINT processed_event_pkey PRIMARY KEY (id),
    CONSTRAINT processed_event_event_id_key UNIQUE (event_id)
);

CREATE TABLE public.retry_event (
    id uuid NOT NULL,
    event_id uuid NOT NULL,
    saga_id uuid,
    payload jsonb NOT NULL,
    attempts integer DEFAULT 0,
    max_attempts integer DEFAULT 5,
    next_retry_at timestamp NOT NULL,
    status character varying(255) NOT NULL,
    last_error character varying(255),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT retry_event_pkey PRIMARY KEY (id)
);

CREATE TABLE public.dlq_event (
    id uuid NOT NULL,
    created_at timestamp,
    payload text,
    reason character varying(255),
    reprocessed boolean NOT NULL,
    next_retry_at timestamp,
    retry_count integer NOT NULL,
    CONSTRAINT dlq_event_pkey PRIMARY KEY (id)
);

CREATE TABLE public.auditoria (
    id uuid NOT NULL,
    entidade character varying(100),
    entidade_id uuid,
    acao character varying(20),
    usuario character varying(100),
    data timestamp DEFAULT CURRENT_TIMESTAMP,
    dados jsonb,
    CONSTRAINT auditoria_pkey PRIMARY KEY (id)
);

CREATE TABLE public.comunicacao (
    id uuid NOT NULL,
    remetente_id uuid NOT NULL,
    destinatario_id uuid NOT NULL,
    tipo character varying(30) NOT NULL,
    mensagem text NOT NULL,
    criado_em timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT comunicacao_pkey PRIMARY KEY (id)
);

CREATE TABLE public.sugestao_conteudo_aula (
    id uuid NOT NULL,
    aluno_id uuid NOT NULL,
    oferta_disciplina_id uuid NOT NULL,
    conteudo text NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE',
    criado_em timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sugestao_conteudo_aula_pkey PRIMARY KEY (id),
    CONSTRAINT sugestao_conteudo_aula_aluno_id_fkey FOREIGN KEY (aluno_id) REFERENCES public.aluno(id),
    CONSTRAINT sugestao_conteudo_aula_oferta_disciplina_id_fkey FOREIGN KEY (oferta_disciplina_id) REFERENCES public.oferta_disciplina(id)
);