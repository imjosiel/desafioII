# Desafio II — CRUD com Spring Boot + PostgreSQL

## Visão Geral
- CRUD com paginação para `produto/serviço`, `pedido` e `item de pedido`.
- IDs `UUID` automáticos em todas as entidades.
- Diferenciação entre produto e serviço via campo `tipo` (`PRODUTO`/`SERVICO`).
- Desconto percentual aplicado apenas sobre o total dos produtos, e somente quando o pedido está `ABERTO`.
- Bloqueios: não excluir produto/serviço associado a pedido; não adicionar produto desativado.
- API REST JSON com Spring Boot, JPA (Hibernate) e PostgreSQL.
- Testes automatizados cobrindo regras principais.

## Tecnologias
- Java 17 (compatível com requisito 8+)
- Maven
- Spring Boot (Web, Data JPA, Validation)
- PostgreSQL
- Hibernate (DDL auto `update`)

## Pré-requisitos
- Java 17+ instalado
- Maven instalado
- Banco PostgreSQL acessível (Neon DB ou local)

## Configuração do Banco

### Neon DB (exemplo)
- Em PowerShell (Windows):
```
$env:DB_URL="jdbc:postgresql://ep-hidden-dust-ahk703uz-pooler.c-3.us-east-1.aws.neon.tech/neondb_2?sslmode=require"
$env:DB_USER="neondb_owner"
$env:DB_PASSWORD="npg_oPpd0A7mxOyn"
```
- Observação: não use `channel_binding=require` em JDBC.

### PostgreSQL local (exemplo)
- Crie um banco e usuário; depois exporte:
```
$env:DB_URL="jdbc:postgresql://localhost:5432/desafioII"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
```

## Executando a Aplicação
- Com Maven:
```
mvn spring-boot:run
```
- Usando JAR (após build):
```
mvn clean package
java -jar target/desafioII-0.0.1-SNAPSHOT.jar
```
- A API sobe em `http://localhost:8080`.

## Variáveis de Ambiente
- `DB_URL`: JDBC do PostgreSQL
- `DB_USER`: usuário
- `DB_PASSWORD`: senha

## Coleções para Teste
- Insomnia: `curls para teste/insomnia_export.json`
- Postman (manual): `curls para teste/postman_collection.json`
- Postman (automatizada com scripts de Test): `curls para teste/postman_collection_automated.json`
- Configure `base_url` = `http://localhost:8080`.

## Fluxo Sugerido de Teste
1. Criar produto: `POST /produtos-servicos` body:
```
{
  "nome": "Notebook",
  "preco": 3000,
  "tipo": "PRODUTO",
  "ativo": true
}
```
2. Criar serviço: `POST /produtos-servicos` body:
```
{
  "nome": "Suporte",
  "preco": 200,
  "tipo": "SERVICO",
  "ativo": true
}
```
3. Criar pedido: `POST /pedidos`
4. Adicionar item (produto): `POST /pedidos/{pedidoId}/itens`
```
{
  "produtoServicoId": "<produtoId>",
  "quantidade": 1
}
```
5. Adicionar item (serviço): `POST /pedidos/{pedidoId}/itens`
```
{
  "produtoServicoId": "<servicoId>",
  "quantidade": 2
}
```
6. Aplicar desconto: `POST /pedidos/{pedidoId}/desconto?percentual=10`
7. Detalhar pedido: `GET /pedidos/{pedidoId}` (confira `totalProdutos`, `totalServicos`, `totalComDesconto`)
8. Fechar pedido: `POST /pedidos/{pedidoId}/fechar`

## Exemplo de Resposta (Neon)
```
GET /pedidos/14d33ec2-c522-4740-99ea-44494a19e2b8

{
  "id": "14d33ec2-c522-4740-99ea-44494a19e2b8",
  "situacao": "ABERTO",
  "descontoPercentual": 10.00,
  "totalProdutos": 0.00,
  "totalServicos": 400.00,
  "totalComDesconto": 400.00
}
```
Observação: como o pedido acima contém apenas serviços, o desconto não altera o total (regra: desconto incide somente sobre produtos).

### Exemplo com produto e serviço (desconto aplicado)
```
GET /pedidos/<uuid-gerado>

{
  "id": "c2c1f6a7-3f3a-4a52-8a6c-2c2f2a1b9b11",
  "situacao": "ABERTO",
  "descontoPercentual": 10.00,
  "totalProdutos": 3000.00,
  "totalServicos": 400.00,
  "totalComDesconto": 3100.00,
  "itens": [
    {
      "id": "8a7c9f80-b9de-4b59-a5b5-6f5f0a5c7b22",
      "nome": "Notebook",
      "precoUnitario": 3000.00,
      "quantidade": 1
    },
    {
      "id": "0b1fcd77-1e2a-4d9c-9c3e-2a7e9c3a7b10",
      "nome": "Suporte",
      "precoUnitario": 200.00,
      "quantidade": 2
    }
  ]
}
```
Observação: o desconto de 10% incide somente sobre `totalProdutos` (3000 → 2700). O `totalComDesconto` combina `totalProdutos` com desconto e `totalServicos` sem desconto (2700 + 400 = 3100).

## Endpoints Principais
- Produto/Serviço
  - `POST /produtos-servicos`
  - `GET /produtos-servicos?size=10&page=0`
  - `GET /produtos-servicos/{id}`
  - `PUT /produtos-servicos/{id}`
  - `DELETE /produtos-servicos/{id}` (bloqueado se associado a pedido)
- Pedido
  - `POST /pedidos`
  - `GET /pedidos?size=10&page=0`
  - `GET /pedidos/{id}` (retorna totais, desconto e itens)
  - `POST /pedidos/{id}/desconto?percentual=10` (somente em `ABERTO`)
  - `POST /pedidos/{id}/fechar`
- Itens de Pedido
  - `POST /pedidos/{id}/itens` (adiciona)
  - `PUT /pedidos/{id}/itens/{itemId}` (atualiza quantidade)
  - `DELETE /pedidos/{id}/itens/{itemId}` (remove)

## Regras de Negócio
- IDs `UUID` automáticos em todas as entidades.
- `tipo` diferencia `PRODUTO` e `SERVICO`.
- Desconto percentual incide apenas sobre o total dos produtos.
- Desconto permitido apenas em pedidos `ABERTO`; `FECHADO` bloqueia.
- Não excluir produto/serviço associado a algum pedido.
- Não adicionar produto desativado em pedido.

## Paginação
- Use `size` e `page` em listagens:
- Ex.: `GET /produtos-servicos?size=10&page=0`

## Testes Automatizados
- Execute:
```
mvn clean test
```
- Os testes cobrem regras de desconto, bloqueios e integração dos endpoints.

## Smoke Test Automatizado (opcional)
- Script PowerShell: `scripts/smoke-test.ps1`
- Exemplo de execução:
```
powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-test.ps1 \ 
  -DbUrl "<jdbc-url>" -DbUser "<usuario>" -DbPass "<senha>"
```
- Imprime `pedido`, `produto`, `servico` e `totalComDesconto` ao finalizar.

## Troubleshooting
- Erro de conexão: verifique `DB_URL`, `DB_USER`, `DB_PASSWORD` e `sslmode=require`.
- `channel_binding=require` não é suportado em JDBC; remova do URL.
- Porta ocupada: ajuste `server.port` se necessário em `application.properties` ou variável `SERVER_PORT`.

## Build
- Empacotar e executar:
```
mvn clean package
java -jar target/desafioII-0.0.1-SNAPSHOT.jar
```