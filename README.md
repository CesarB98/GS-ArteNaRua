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
