# Room Reservation API

API REST desenvolvida em **Spring Boot** para gerenciamento de reservas de salas. O sistema permite cadastrar usuários e salas, e realizar reservas aplicando um conjunto de regras de negócio (horário de funcionamento, conflito de horários, capacidade da sala, status da sala, entre outras).

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Arquitetura e organização do código](#arquitetura-e-organização-do-código)
- [Modelo de domínio](#modelo-de-domínio)
- [Regras de negócio](#regras-de-negócio)
- [Tratamento de erros](#tratamento-de-erros)
- [Endpoints da API](#endpoints-da-api)
- [Como executar o projeto](#como-executar-o-projeto)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Testes](#testes)
- [Roadmap / Próximos passos](#roadmap--próximos-passos)
- [Autor](#autor)

## Sobre o projeto

O **Room Reservation API** simula um sistema de reserva de salas de reunião, no estilo de sistemas corporativos de agendamento de espaços. Foi desenvolvido como projeto de portfólio para consolidar conceitos de desenvolvimento backend com Java e Spring Boot, com foco em:

- Modelagem de domínio com JPA/Hibernate;
- Separação de responsabilidades em camadas (Controller → Service → Repository);
- Uso de DTOs (Java Records) para entrada e saída de dados;
- Implementação de regras de negócio não triviais (validação de conflitos de horário, capacidade, janela de funcionamento);
- Tratamento de erros centralizado com `@RestControllerAdvice`;
- Testes unitários da camada de serviço com JUnit 5 e Mockito;
- Documentação de API com springdoc-openapi (Swagger).

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 4.0.7**
  - Spring Web (MVC)
  - Spring Data JPA
  - Bean Validation (Jakarta Validation)
- **PostgreSQL** — banco de dados relacional
- **Docker Compose** — subida do banco de dados local (via `spring-boot-docker-compose`)
- **Lombok** — redução de boilerplate (utilizado no `ErrorResponse`)
- **springdoc-openapi** — geração automática de documentação Swagger/OpenAPI
- **JUnit 5** e **Mockito** (estilo BDD, com `BDDMockito`) — testes unitários da camada de serviço
- **Maven** — gerenciador de dependências e build

## Arquitetura e organização do código

O projeto segue uma arquitetura em camadas tradicional do ecossistema Spring:

```
src/main/java/br/com/jhohannesfreitas/roomreservationapi/
├── controller/       # Camada de apresentação (endpoints REST)
├── service/          # Regras de negócio
├── repository/       # Acesso a dados (Spring Data JPA)
├── domain/
│   ├── entity/        # Entidades JPA (Usuario, Sala, Reserva)
│   └── enums/          # Enums de domínio (StatusSala, StatusReserva)
├── dto/                # Records de entrada (Request) e saída (Response)
├── mapper/             # Conversão entre Entity <-> DTO
└── exception/           # Exceções customizadas e tratamento global de erros
```

Fluxo padrão de uma requisição:

```
Controller → valida entrada (@Valid) → Service → aplica regras de negócio
   → Repository → banco de dados → Mapper → Response DTO → Controller
```

## Modelo de domínio

O sistema é composto por três entidades principais:

### `Usuario`
Representa a pessoa que realiza reservas.
- `id`, `nome`, `email` (único), `senha`
- Relacionamento `@OneToMany` com `Reserva`

### `Sala`
Representa uma sala disponível para reserva.
- `id`, `numero` (único), `capacidade`, `status` (`LIVRE` | `OCUPADA`)
- Relacionamento `@OneToMany` com `Reserva`

### `Reserva`
Representa o agendamento de uma sala por um usuário em um intervalo de tempo.
- `id`, `data`, `horaInicial`, `horaFinal`, `quantidadePessoas`, `status` (`ATIVA` | `CANCELADA`)
- Relacionamento `@ManyToOne` com `Usuario` e com `Sala`

## Regras de negócio

As validações abaixo são aplicadas na camada de serviço (`ReservaService`, `SalaService`, `UsuarioService`) tanto no cadastro quanto na atualização, quando aplicável:

**Usuário**
- E-mail deve ser único no sistema.

**Sala**
- Número da sala deve ser único.
- Capacidade deve ser um valor positivo.

**Reserva**
- Usuário e sala informados devem existir.
- A sala deve estar com status `LIVRE`.
- Não é permitido reservar em datas passadas.
- O horário da reserva deve estar dentro do horário de funcionamento (08:00 às 18:00).
- A hora inicial deve ser anterior à hora final.
- A quantidade de pessoas não pode exceder a capacidade da sala.
- Não é permitido conflito de horário entre reservas ativas da mesma sala. A verificação usa um **intervalo semiaberto `[início, fim)`**, ou seja, uma reserva pode começar exatamente no horário em que outra termina (ex.: reserva existente `10:00–12:00` e nova reserva `12:00–14:00` são compatíveis).
- Na atualização, a própria reserva é ignorada na checagem de conflito de horário.
- Reservas com status `CANCELADA` não entram na verificação de conflito.

## Tratamento de erros

O projeto centraliza o tratamento de exceções em um `GlobalExceptionHandler` (`@RestControllerAdvice`), retornando um corpo padronizado (`ErrorResponse`) com `status`, `error`, `message` e `timestamp`.

| Exceção | Status HTTP | Cenário |
|---|---|---|
| `RegraNegocioException` | Definido dinamicamente (400, 404 ou 409) | Violação de regra de negócio (ex.: sala não encontrada, conflito de horário) |
| `MethodArgumentTypeMismatchException` | 400 | Parâmetro de requisição com tipo inválido |
| `AccessDeniedException` | 403 | Acesso não autorizado a um recurso |
| `Exception` (genérica) | 500 | Erro inesperado no servidor |

A exceção `RegraNegocioException` carrega seu próprio `HttpStatus`, permitindo que a mesma classe represente diferentes tipos de erro de negócio (não encontrado, conflito, requisição inválida) sem a necessidade de múltiplas classes de exceção.

## Endpoints da API

Base path: `/api/v1`

### Usuários — `/api/v1/usuarios`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/usuarios` | Cadastra um novo usuário |
| `GET` | `/usuarios` | Lista todos os usuários |
| `GET` | `/usuarios/paginado` | Lista usuários com paginação e ordenação |
| `GET` | `/usuarios/{id}` | Busca um usuário por ID |
| `PUT` | `/usuarios/{id}` | Atualiza um usuário existente |
| `DELETE` | `/usuarios/{id}` | Remove um usuário |

### Salas — `/api/v1/salas`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/salas` | Cadastra uma nova sala |
| `GET` | `/salas` | Lista todas as salas |
| `GET` | `/salas/paginado` | Lista salas com paginação e ordenação |
| `GET` | `/salas/{id}` | Busca uma sala por ID |
| `PUT` | `/salas/{id}` | Atualiza uma sala existente |
| `DELETE` | `/salas/{id}` | Remove uma sala |

### Reservas — `/api/v1/reservas`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/reservas` | Cadastra uma nova reserva |
| `GET` | `/reservas` | Lista todas as reservas |
| `GET` | `/reservas/paginado` | Lista reservas com paginação e ordenação |
| `GET` | `/reservas/sala/{id}?inicio=&fim=` | Lista reservas ativas de uma sala em um intervalo de datas (paginado) |
| `GET` | `/reservas/{id}` | Busca uma reserva por ID |
| `PUT` | `/reservas/{id}` | Atualiza uma reserva existente |
| `DELETE` | `/reservas/{id}` | Remove uma reserva |

**Exemplo — cadastro de reserva:**

```http
POST /api/v1/reservas
Content-Type: application/json

{
  "usuarioId": 1,
  "salaId": 1,
  "data": "2026-08-10",
  "horaInicial": "09:00:00",
  "horaFinal": "10:30:00",
  "quantidadePessoas": 4
}
```

**Exemplo — listagem paginada e ordenada:**

```http
GET /api/v1/reservas/paginado?page=0&size=10&sort=data,desc
```

Uma coleção pronta para uso no **Insomnia** com exemplos de todas as requisições está disponível em `requests/`.

## Como executar o projeto

### Pré-requisitos

- Java 21+
- Maven (ou usar o wrapper `./mvnw` incluso no projeto)
- Docker (para subir o PostgreSQL via `compose.yaml`) — ou uma instância PostgreSQL própria

### Variáveis de ambiente

A aplicação lê a configuração do banco a partir de variáveis de ambiente:

```
DB_HOST=localhost:5432
DB_NAME=roomreservation
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
```

### Passo a passo

```bash
# 1. Clonar o repositório
git clone <url-do-repositorio>
cd roomreservationapi

# 2. Subir o banco de dados (se for usar o compose.yaml)
docker compose up -d

# 3. Definir as variáveis de ambiente (ou usar um .env / IDE run configuration)

# 4. Rodar a aplicação
./mvnw spring-boot:run
```

A aplicação sobe por padrão na porta `8080`.

> **Nota:** por padrão, `spring.docker.compose.enabled` está definido como `false` no `application.yaml`, então o Docker Compose não é acionado automaticamente — suba o container manualmente com `docker compose up -d` antes de iniciar a aplicação, ou ajuste essa propriedade conforme sua necessidade.

## Documentação interativa (Swagger)

Com a aplicação em execução, a documentação OpenAPI/Swagger UI fica disponível em:

```
http://localhost:8080/swagger-ui/index.html
```

Todos os endpoints estão documentados com descrições de regras de negócio, parâmetros e possíveis códigos de resposta HTTP, usando as anotações `@Operation` e `@ApiResponses` do springdoc.

## Testes

O projeto conta atualmente com **testes unitários da camada de serviço** (`ReservaServiceTest`, `SalaServiceTest`, `UsuarioServiceTest`), escritos com **JUnit 5** e **Mockito** (estilo `BDDMockito`), cobrindo tanto os fluxos de sucesso quanto as principais regras de negócio e cenários de erro — incluindo cadastro, listagem (simples e paginada), busca por ID, atualização e exclusão, além de casos como conflito de horário, sala/usuário inexistente, sala ocupada, data no passado, horário fora do expediente e capacidade excedida.

```bash
# Executar todos os testes
./mvnw test
```

> **Sobre a cobertura de testes:** os testes da camada de **Controller** ainda não foram implementados neste projeto — este é um ponto conhecido e já planejado como próxima etapa (ver [Roadmap](#roadmap--próximos-passos)). A cobertura atual está concentrada na camada de Service, onde reside a maior parte da complexidade e das regras de negócio da aplicação.

## Roadmap / Próximos passos

- [ ] Implementar testes de integração da camada de **Controller** (`@WebMvcTest` / `MockMvc`), cobrindo validação de payload, status HTTP e contrato dos endpoints.
- [ ] Adicionar testes de integração ponta a ponta com banco de dados em memória ou Testcontainers.
- [ ] Adicionar autenticação e autorização (Spring Security + JWT).
- [ ] Adicionar migrations versionadas com Flyway (atualmente o schema é gerado via `ddl-auto: update`).
- [ ] Criptografar a senha do usuário (atualmente armazenada em texto plano).

## Autor

**Jhohannes Freitas**
Desenvolvedor Java em formação, focado em Spring Boot e back-end.
