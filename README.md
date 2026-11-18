# ArteNaRua — Global Solution 2026-02 (Microservice and Web Engineering)

## Integrantes

| Nome | RM |
|------|----|
| Caio Leria | 557833 |
| Cesar Brasil | 556236 |

## Problema

A arte urbana (grafites, murais, esculturas) e o patrimônio cultural das cidades passam despercebidos: as obras ficam
sem contexto (quem fez, quando, por quê) e sem acessibilidade para pessoas com deficiência visual.

## Solução

O **ArteNaRua** cataloga obras de arte urbana com ficha completa (artista, história, localização e **audiodescrição**).
A comunidade sugere obras novas e um moderador aprova ou rejeita. Esta entrega é o backend em microsserviços, em um
único repositório (monorepo):

| Componente | Porta | Papel |
|------------|-------|-------|
| `eureka-server` | **8761** | Registro e descoberta de serviços |
| `api-gateway` | **8082** | Porta de entrada única, rotas `lb://` com Load Balancer, **Swagger UI** |
| `ms-obras` | aleatória | Catálogo de artistas e obras, busca por proximidade (banco `db_obras`) |
| `ms-contribuicoes` | aleatória | Sugestões de obras e moderação (banco `db_contribuicoes`) |
| MySQL 8.4 (Docker) | **3307** | Um banco independente por microsserviço |

### Comunicação síncrona

Ao **aprovar** uma contribuição, o `ms-contribuicoes` chama o `ms-obras` por **OpenFeign** (pelo nome do serviço no
Eureka, sem URL fixa, balanceado pelo Spring Cloud LoadBalancer): cria a obra e a publica
(`Obra.status`: `EM_MODERACAO` → `PUBLICADA`). Só depois a contribuição passa de `PENDENTE` para `APROVADA`.

Se o `ms-obras` estiver fora do ar, o **Circuit Breaker** (Resilience4j) aciona o **fallback**: a resposta é
`503` com mensagem clara e a contribuição continua `PENDENTE`, podendo ser aprovada de novo depois.

---

# Como executar (passo a passo)

> Siga **na ordem**. Tempo total: cerca de 5 minutos (na primeira vez o Maven baixa dependências, então precisa de
> internet e pode demorar mais). Você vai usar **5 janelas de terminal**: 1 para o Docker e 4 para os serviços
> (que ficam rodando, não feche).

## Passo 0 — Conferir o que precisa estar instalado

Abra um terminal e rode:

```bash
java -version
docker --version
git --version
```

- `java -version` **precisa mostrar a versão 25** (ex.: `openjdk version "25.0.3"`). O projeto exige Java 25.
  Se mostrar outra versão, veja **"Se o Java não for o 25"** no Passo 3.
- O **Docker Desktop precisa estar aberto e rodando** (ícone da baleia sem erro).
- As portas **3307, 8761 e 8082** precisam estar livres.

## Passo 1 — Baixar o projeto

```bash
git clone https://github.com/CesarB98/GS-ArteNaRua.git
cd GS-ArteNaRua
```

Todos os comandos a seguir são executados **dentro da pasta `GS-ArteNaRua`** (a raiz do repositório).

## Passo 2 — Subir o MySQL (Docker)

```bash
docker compose up -d
```

Isso cria o MySQL na porta **3307** (usuário `root`, senha `root`) com os bancos `db_obras` e `db_contribuicoes`.

**Aguarde uns 30 segundos** e confira se os bancos existem:

```bash
docker exec artenarua-mysql mysql -uroot -proot -e "show databases;"
```

Deve aparecer `db_contribuicoes` e `db_obras` na lista. Se não aparecer, espere mais um pouco e repita.

> As tabelas e os dados iniciais **não** são criados aqui: o Flyway cria tudo sozinho quando cada microsserviço sobe.

## Passo 3 — Subir os 4 serviços (um terminal para cada, nesta ordem)

Abra **4 terminais novos**, todos na pasta `GS-ArteNaRua`. Em cada um, rode o comando indicado e **espere aparecer
a linha `Started ... in ... seconds`** antes de ir para o próximo.

**Terminal A — Eureka Server** (espere a mensagem `Started EurekaServerApplication`):

| Sistema | Comando |
|---------|---------|
| Windows PowerShell | `cd eureka-server` e depois `.\mvnw.cmd spring-boot:run` |
| Windows CMD | `cd eureka-server` e depois `mvnw.cmd spring-boot:run` |
| Linux / macOS / Git Bash | `cd eureka-server && ./mvnw spring-boot:run` |

**Terminal B — ms-obras** (espere `Started MsObrasApplication`): mesmo comando, na pasta `ms-obras`.

**Terminal C — ms-contribuicoes** (espere `Started MsContribuicoesApplication`): mesmo comando, na pasta `ms-contribuicoes`.

**Terminal D — API Gateway** (espere `Started ApiGatewayApplication`): mesmo comando, na pasta `api-gateway`.

Exemplo completo no Windows PowerShell (Terminal B):

```powershell
cd ms-obras
.\mvnw.cmd spring-boot:run
```

Exemplo completo no Linux/macOS/Git Bash (Terminal B):

```bash
cd ms-obras
./mvnw spring-boot:run
```

## Passo 4 — Conferir que está tudo no ar

Abra no navegador: **<http://localhost:8761>** (painel do Eureka).

Na tabela **"Instances currently registered with Eureka"** devem aparecer **3 aplicações com status `UP`**:
`API-GATEWAY`, `MS-OBRAS` e `MS-CONTRIBUICOES`.

- Pode levar **até 1 minuto** depois que os serviços sobem para todos aparecerem. Atualize a página (F5).
- Uma faixa vermelha dizendo *"EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP..."* é **normal** em
  ambiente de desenvolvimento e pode ser ignorada.

## Passo 5 — Testar pelo Swagger (navegador)

Abra **<http://localhost:8082/swagger-ui.html>** (o Swagger fica no **Gateway, porta fixa 8082**).

1. No topo da página, à direita, há uma lista **"Select a definition"**. Escolha **`ms-contribuicoes`** ou
   **`ms-obras`** para alternar entre a documentação de cada serviço.
2. Clique em um endpoint para abri-lo, clique em **"Try it out"** e depois em **"Execute"**. A resposta aparece
   logo abaixo (código HTTP e corpo).

**Exemplo guiado (aprovar uma contribuição e ver a obra publicada):**

1. Em **`ms-contribuicoes`**, abra `GET /api/v1/contribuicoes` → *Try it out* → *Execute*. Veja as contribuições
   `PENDENTE` (anote o `id` de uma delas, ex.: `1`).
2. Abra `PATCH /api/v1/contribuicoes/{id}/aprovar` → *Try it out* → digite o `id` (ex.: `1`) → *Execute*.
   Resultado esperado: **HTTP 200**, `status: APROVADA` e um `obraId` preenchido (o ms-contribuicoes chamou o
   ms-obras por OpenFeign).
3. Troque a lista do topo para **`ms-obras`**, abra `GET /api/v1/obras/{id}` → *Try it out* → digite o `obraId`
   do passo anterior → *Execute*. Resultado esperado: a obra com `status: PUBLICADA`.

> Se aparecer **"Failed to load API definition"**, os serviços ainda não terminaram de se registrar no Eureka:
> espere 30 a 60 segundos e atualize a página (F5).

## Passo 6 — Testar pelo terminal

Abra **mais um terminal** (pode ser em qualquer pasta). Todas as chamadas passam pelo Gateway (`http://localhost:8082`).
No Windows, use `curl.exe` (com `.exe`, porque `curl` sozinho é um apelido do PowerShell) ou `Invoke-RestMethod`.

> Se logo depois de subir tudo o passo 4 (aprovar) retornar **503**, os serviços ainda não se enxergaram pelo Eureka:
> aguarde 30 a 60 segundos e repita.

**1) Listar as obras publicadas perto do Beco do Batman** (esperado: **200**):

```bash
curl -i "http://localhost:8082/api/v1/obras/proximas?lat=-23.5558&lng=-46.6869&raioMetros=300"
```

**2) Listar as contribuições pendentes** (esperado: **200**). Anote o `id` de uma delas (ex.: `1`):

```bash
curl -i "http://localhost:8082/api/v1/contribuicoes?status=PENDENTE"
```

**3) Criar uma contribuição nova** (esperado: **201**, `status: PENDENTE` e o cabeçalho `Location`):

Linux / macOS / Git Bash:

```bash
curl -i -X POST http://localhost:8082/api/v1/contribuicoes -H "Content-Type: application/json" -d '{"titulo":"Mural de Teste","ano":2025,"historia":"Criada pelo terminal.","audiodescricao":"Mural colorido numa parede.","latitude":-23.556,"longitude":-46.687,"endereco":"Beco do Batman, Sao Paulo","tipo":"MURAL","artistaId":1}'
```

Windows PowerShell:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8082/api/v1/contribuicoes -ContentType "application/json" -Body '{"titulo":"Mural de Teste","ano":2025,"historia":"Criada pelo terminal.","audiodescricao":"Mural colorido numa parede.","latitude":-23.556,"longitude":-46.687,"endereco":"Beco do Batman, Sao Paulo","tipo":"MURAL","artistaId":1}'
```

**4) Aprovar a contribuição** (troque o `1` pelo `id` do passo 2). Esperado: **200**, `status: APROVADA` e um `obraId`
preenchido (o ms-contribuicoes chamou o ms-obras por OpenFeign):

```bash
curl -i -X PATCH http://localhost:8082/api/v1/contribuicoes/1/aprovar
```

**5) Ver a obra criada no ms-obras** (troque o `5` pelo `obraId` do passo 4). Esperado: **200** e `status: PUBLICADA`:

```bash
curl -i http://localhost:8082/api/v1/obras/5
```

**6) Casos de erro**

| Chamada | Resultado esperado |
|---------|--------------------|
| `curl -i -X PATCH http://localhost:8082/api/v1/contribuicoes/1/aprovar` (de novo, já aprovada) | **409** |
| `curl -i http://localhost:8082/api/v1/obras/9999` | **404** |

> **Nota:** no navegador só funcionam os `GET` (barra de endereço). `POST` e `PATCH` precisam do **Swagger** (Passo 5)
> ou do terminal (este passo).


## Passo 7 — Parar tudo e começar do zero

- Pare os serviços com **Ctrl+C** em cada terminal.
- Para parar o MySQL **e apagar os dados** (volta ao estado inicial, com a carga do Flyway):

```bash
docker compose down -v
```

---

# Referência

## Endpoints (via Gateway, `http://localhost:8082`)

**ms-obras**

| Método | Rota | Descrição |
|--------|------|-----------|
| GET/POST | `/api/v1/artistas` | Lista / cria artista |
| GET/PUT/DELETE | `/api/v1/artistas/{id}` | Busca / atualiza / exclui artista |
| GET/POST | `/api/v1/obras` | Lista / cria obra (nasce `EM_MODERACAO`) |
| GET | `/api/v1/obras/proximas?lat=&lng=&raioMetros=100` | Obras publicadas próximas |
| GET/PUT/DELETE | `/api/v1/obras/{id}` | Busca / atualiza / exclui obra |
| PATCH | `/api/v1/obras/{id}/status` | Altera o status (usado pelo ms-contribuicoes) |

**ms-contribuicoes**

| Método | Rota | Descrição |
|--------|------|-----------|
| GET/POST | `/api/v1/contribuicoes` | Lista (filtro `?status=`) / cria (nasce `PENDENTE`) |
| GET/PUT/DELETE | `/api/v1/contribuicoes/{id}` | Busca / atualiza (pendente) / exclui (não aprovada) |
| PATCH | `/api/v1/contribuicoes/{id}/aprovar` | **Chama o ms-obras (OpenFeign)** e aprova |
| PATCH | `/api/v1/contribuicoes/{id}/rejeitar` | Rejeita com `{"motivo": "..."}` |

## Documentação OpenAPI

- Swagger UI unificado: <http://localhost:8082/swagger-ui.html>
- Contrato OpenAPI de cada serviço (via Gateway): `http://localhost:8082/ms-obras/v3/api-docs` e
  `http://localhost:8082/ms-contribuicoes/v3/api-docs`

## Tecnologias

Java 25, Spring Boot 4.0.9, Spring Cloud 2025.1.4 (Eureka, Gateway, OpenFeign, LoadBalancer, Resilience4j),
Spring Data JPA, Flyway, MySQL 8.4, springdoc-openapi (Swagger UI), Docker Compose.

## Estrutura

```
eureka-server/      Eureka Server (8761)
api-gateway/        API Gateway (8082) + Swagger UI unificado
ms-obras/           entity · dto (request/response) · repository · service · controller · exception · config
ms-contribuicoes/   entity · dto · client (Feign + fallback) · repository · service · controller · exception · config
docker-compose.yml  MySQL (3307) + criação dos bancos em docker/mysql-init
```
