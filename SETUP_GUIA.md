# 🎓 Academic Core Service - Guia de Setup e Execução

## 📋 Resumo das Correções Aplicadas

### Problemas Resolvidos

1. **Erro do Docker Compose**
   - `DockerProcessStartException: Unable to start docker process`
   - **Causa**: Spring Boot tentava subir Docker Compose automaticamente, mas Docker não estava instalado
   - **Solução**: Desabilitado o auto-start do Docker Compose no `application.yaml`

2. **Banco de Dados Inexistente**
   - `FATAL: banco de dados "db_gestao_escolar" não existe`
   - **Causa**: PostgreSQL rodando, mas banco específico não criado
   - **Solução**: Configurado fallback para banco padrão `postgres` com variáveis de ambiente customizáveis

3. **Kafka não disponível no boot**
   - **Causa**: Listeners Kafka tentavam conectar na largada sem broker disponível
   - **Solução**: Desabilitado auto-startup dos listeners por padrão

---

## 🚀 Modo 1: Execução Local (SEM Docker)

Ideal para desenvolvimento rápido sem infraestrutura.

### Pré-requisitos

- JDK 21 instalado: `C:\Program Files\java\jdk-21.0.9`
- PostgreSQL rodando em `localhost:5433` (opcional - app usa `postgres` como fallback)
- Maven (via `mvnw.cmd`)

### Passos

1. **Abra o terminal PowerShell**

```powershell
cd "C:\Projeto\git-meu\academic-core-service"
```

2. **Configure o JDK 21 (se não estiver em PATH)**

```powershell
$env:JAVA_HOME="C:\Program Files\java\jdk-21.0.9"
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
```

3. **Inicie a aplicação**

```powershell
.\mvnw.cmd -DskipTests spring-boot:run
```

### Resultado Esperado

```
Tomcat initialized with port 8080 (http)
...
Started AcademicCoreServiceApplication in X.XXX seconds
```

### Validação

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/actuator/health
```

Resposta: `{"status":"UP"}`

---

## 🐳 Modo 2: Execução com Docker Compose (COM Docker Desktop)

**Esta é a configuração recomendada para outra máquina com Docker Desktop**.

### Pré-requisitos

- ✅ Docker Desktop instalado e rodando
- ✅ PowerShell ou terminal disponível
- ✅ JDK 21 configurado (mesmo que o Docker rode os serviços)

### Passo 1: Validar Docker

```powershell
docker --version
docker compose version
```

Ambos devem retornar versões.

### Passo 2: Subir a Infraestrutura

```powershell
cd "C:\Projeto\git-meu\academic-core-service"
docker compose up -d
```

Isso irá subir:
- **PostgreSQL 16** em `localhost:5433`
  - Banco: `db_gestao_escolar`
  - Usuário: `postgres`
  - Senha: `root123`
  - Volume persistente: `postgres_data`

- **Zookeeper** em `localhost:2181`

- **Kafka** em `localhost:9092`
  - Tópico: `MATRICULA`
  - Consumer Group: `academic-core-test`

### Passo 3: Verificar Status dos Containers

```powershell
docker compose ps
```

Resultado esperado:

```
NAME            STATUS              PORTS
academic-core-service-postgres-1   Up (healthy)   0.0.0.0:5433->5432/tcp
academic-core-service-zookeeper-1  Up (healthy)   
academic-core-service-kafka-1      Up (healthy)   0.0.0.0:9092->9092/tcp
```

**Importante**: Aguarde todos ficarem `(healthy)` antes de iniciar a app.

### Passo 4: Configurar Variáveis de Ambiente

```powershell
$env:JAVA_HOME="C:\Program Files\java\jdk-21.0.9"
$env:Path="$env:JAVA_HOME\bin;" + $env:Path

# Ativar o banco do projeto
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/db_gestao_escolar"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="root123"

# Ativar Kafka listeners
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:SPRING_KAFKA_LISTENER_AUTO_STARTUP="true"
```

### Passo 5: Inicie a Aplicação

```powershell
cd "C:\Projeto\git-meu\academic-core-service"
.\mvnw.cmd -DskipTests spring-boot:run
```

### Passo 6: Validação Completa

```powershell
# Health check
Invoke-WebRequest -UseBasicParsing http://localhost:8080/actuator/health | Select-Object -ExpandProperty Content

# Logs do Kafka (opcional)
docker compose logs kafka

# Conectar ao Postgres (opcional)
docker compose exec postgres psql -U postgres -d db_gestao_escolar -c "\dt"
```

---

## 📝 Variáveis de Ambiente

### Padrão (Local sem Docker)

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root123
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_LISTENER_AUTO_STARTUP=false
```

### Com Docker Compose

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/db_gestao_escolar
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root123
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_LISTENER_AUTO_STARTUP=true
```

---

## 🛑 Parar os Containers

### Parar mas manter dados

```powershell
docker compose stop
```

### Parar e remover tudo (menos volumes)

```powershell
docker compose down
```

### Parar, remover tudo E volumes

```powershell
docker compose down -v
```

---

## 📊 Verificação de Conectividade

### PostgreSQL

```powershell
# Dentro do container
docker compose exec postgres psql -U postgres -c "SELECT version();"

# Ou via conexão remota (se tiver pgAdmin ou similar)
# Host: localhost:5433
# User: postgres
# Password: root123
# Database: db_gestao_escolar
```

### Kafka

```powershell
# Listar tópicos
docker compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver listeners
docker compose logs kafka | Select-String "Registered new cluster ID"
```

### Aplicação

```powershell
# Health (sem autenticação)
curl http://localhost:8080/actuator/health

# Metrics (opcional)
curl http://localhost:8080/actuator/metrics

# DLQ (se houver endpointuri)
curl http://localhost:8080/dlq
```

---

## 🔧 Configuração do `compose.yaml`

O arquivo já está pré-configurado com:

- ✅ **PostgreSQL 16** com volume persistente
- ✅ **Zookeeper** com healthcheck
- ✅ **Kafka** com dependência de Zookeeper e healthcheck
- ✅ Todas as credenciais do `application.yaml`
- ✅ Porta `5433` mapeada para PostgreSQL (compatível com app.yaml)

**Não é necessário editar o `compose.yaml`** para uso padrão.

---

## 📌 Diferenças Entre os Modos

| Aspecto | Modo Local | Modo Docker |
|---------|-----------|-----------|
| **Docker necessário?** | ❌ Não | ✅ Sim |
| **PostgreSQL** | Precisa instalar | 🐳 No container |
| **Kafka** | ❌ Não está disponível | ✅ No container |
| **Persistência de dados** | Depende da instalação | ✅ Volume Docker |
| **Listeners Kafka ativos?** | ❌ Não | ✅ Sim |
| **Ideal para** | Dev rápido | Teste integração completa |

---

## 🐛 Troubleshooting

### Erro: "docker: command not found"

**Solução**: Docker não está instalado ou não está em PATH.
- Instale Docker Desktop
- Reinicie o terminal após instalar

### Erro: "Cannot connect to Postgres"

**Solução**: PostgreSQL no container ainda não está ready.

```powershell
# Aguarde o healthcheck
docker compose ps

# Se ainda houver problema
docker compose logs postgres
```

### Erro: "Unable to determine Dialect without JDBC metadata"

**Solução**: Verifique se o banco PostgreSQL está realmente disponível.

```powershell
# Confirme que postgres está UP
docker compose ps postgres

# Tente conectar
docker compose exec postgres psql -U postgres -c "\l"
```

### Erro: "Connection refused on Kafka"

**Solução**: Kafka precisa estar ready. Aguarde.

```powershell
# Ver logs do Kafka
docker compose logs kafka

# Se persistir, restart
docker compose restart kafka
```

### App inicia mas sem conectar ao banco

**Solução**: Verifique as variáveis de ambiente.

```powershell
# Validar que as variáveis foram setadas
$env:SPRING_DATASOURCE_URL
$env:SPRING_KAFKA_LISTENER_AUTO_STARTUP

# Se vazio, rodar novamente com as variáveis definidas
```

---

## 📄 Arquivos Alterados

### 1. `application.yaml`
- ✅ Adicionado: `spring.docker.compose.enabled: false`
- ✅ Adicionado: suporte a variáveis de ambiente para datasource e Kafka
- ✅ Adicionado: `spring.kafka.listener.auto-startup: false` como padrão

### 2. `compose.yaml`
- ✅ Adicionado: serviço PostgreSQL com volume persistente
- ✅ Adicionado: healthchecks para todos os serviços
- ✅ Adicionado: dependência de healthcheck entre serviços

### 3. `pom.xml`
- ✅ Adicionado: `maven.compiler.release`, `maven.compiler.source`, `maven.compiler.target` = Java 21
- ✅ Mantido: todas as dependências (Spring Boot 3.5.13, Spring Kafka, etc.)

### 4. `.mvn/wrapper/maven-wrapper.properties`
- ✅ Corrigido: distributionUrl e wrapperUrl válidas

---

## ✅ Checklist de Deployment

### Antes de rodar em nova máquina com Docker Desktop

- [ ] Docker Desktop instalado e rodando
- [ ] JDK 21 instalado e em PATH
- [ ] Clonar/copiar o projeto
- [ ] Navegar até a pasta do projeto
- [ ] Rodar `docker compose up -d`
- [ ] Aguardar healthchecks ficarem GREEN
- [ ] Definir variáveis de ambiente (ou usar as do arquivo deste guia)
- [ ] Rodar `.\mvnw.cmd -DskipTests spring-boot:run`
- [ ] Validar em `http://localhost:8080/actuator/health`

---

## 📞 Resumo Rápido

### Setup Local (5 min)

```powershell
$env:JAVA_HOME="C:\Program Files\java\jdk-21.0.9"
cd "C:\Projeto\git-meu\academic-core-service"
.\mvnw.cmd -DskipTests spring-boot:run
```

### Setup com Docker (10 min)

```powershell
cd "C:\Projeto\git-meu\academic-core-service"
docker compose up -d
# Aguardar 30-40s para todos ficarem healthy
$env:JAVA_HOME="C:\Program Files\java\jdk-21.0.9"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/db_gestao_escolar"
$env:SPRING_KAFKA_LISTENER_AUTO_STARTUP="true"
.\mvnw.cmd -DskipTests spring-boot:run
```

---

## 🎯 Próximos Passos (Opcional)

- Criar `application-local.yaml` e `application-docker.yaml` para separar perfis
- Adicionar kafdrop para visualizar tópicos Kafka
- Configurar CI/CD com Docker build
- Implementar script de inicialização automática

---

**Documento gerado em**: 2026-04-20  
**Versão do projeto**: 0.0.1-SNAPSHOT  
**Java**: 21  
**Spring Boot**: 3.5.13  
**PostgreSQL**: 16  
**Kafka**: 7.5.0

