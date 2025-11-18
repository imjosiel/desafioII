#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

prod_json=$(curl -sS -X POST "$BASE_URL/produtos-servicos" \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Notebook","preco":3000,"tipo":"PRODUTO","ativo":true}')
prod_id=$(echo "$prod_json" | grep -oE '"id"\s*:\s*"[^"]+"' | sed -E 's/.*"id"\s*:\s*"([^"]+)".*/\1/')

serv_json=$(curl -sS -X POST "$BASE_URL/produtos-servicos" \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Suporte","preco":200,"tipo":"SERVICO","ativo":true}')
serv_id=$(echo "$serv_json" | grep -oE '"id"\s*:\s*"[^"]+"' | sed -E 's/.*"id"\s*:\s*"([^"]+)".*/\1/')

pedido_json=$(curl -sS -X POST "$BASE_URL/pedidos")
pedido_id=$(echo "$pedido_json" | grep -oE '"id"\s*:\s*"[^"]+"' | sed -E 's/.*"id"\s*:\s*"([^"]+)".*/\1/')

curl -sS -X POST "$BASE_URL/pedidos/$pedido_id/itens" \
  -H 'Content-Type: application/json' \
  -d '{"produtoServicoId":"'$prod_id'","quantidade":1}' >/dev/null

curl -sS -X POST "$BASE_URL/pedidos/$pedido_id/itens" \
  -H 'Content-Type: application/json' \
  -d '{"produtoServicoId":"'$serv_id'","quantidade":2}' >/dev/null

curl -sS -X POST "$BASE_URL/pedidos/$pedido_id/desconto?percentual=10" >/dev/null

view_json=$(curl -sS "$BASE_URL/pedidos/$pedido_id")
echo "$view_json"