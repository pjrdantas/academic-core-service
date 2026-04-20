# academic-core-service

API responsável pelo núcleo acadêmico de matrícula em arquitetura orientada a eventos.

## Finalidade da API

O `academic-core-service` centraliza o fluxo de matrícula escolar, incluindo:

- criação e atualização de matrícula;
- orquestração de eventos de matrícula/pagamento via Kafka;
- confiabilidade de processamento com idempotência, retry e DLQ;
- persistência transacional com PostgreSQL.

Em termos de negócio, o serviço garante que eventos de matrícula sejam processados com segurança, rastreabilidade e resiliência.

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.5.13
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Kafka
- PostgreSQL
- Actuator (observabilidade)
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Docker Compose (para ambiente local com Kafka/Postgres)

## O que precisa para funcionar

### Pré-requisitos

- JDK 21 instalado
- Docker Desktop (para subir Kafka/Postgres via `compose.yaml`) ou serviços equivalentes já disponíveis
- Acesso às portas padrão:
  - `9092` (Kafka)
  - `5432` no ambiente principal (ou `5433` no perfil local)

### Configuração principal (padrão)

O arquivo `src/main/resources/application.yaml` é a configuração principal da API (ambiente alvo).

### Configuração local desta máquina

O arquivo `src/main/resources/application-local.yaml` contém a configuração secundária para execução local neste repositório.

O script `start.bat` já inicia com perfil local (`SPRING_PROFILES_ACTIVE=local`).

## Como executar

### 1) Subir infraestrutura com Docker

```powershell
Set-Location "C:\Projeto\git-meu\academic-core-service"
docker compose up -d
```

### 2) Executar com perfil local (Windows)

```powershell
Set-Location "C:\Projeto\git-meu\academic-core-service"
.\start.bat
```

### 3) Executar com configuração principal

```powershell
Set-Location "C:\Projeto\git-meu\academic-core-service"
mvnw.cmd spring-boot:run
```

## Saúde da aplicação

Depois de subir, valide em:

- `GET /actuator/health`
