param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = 'Stop'

$prodDto = @{ nome = 'Notebook'; preco = 3000; tipo = 'PRODUTO'; ativo = $true } | ConvertTo-Json
$prod = Invoke-RestMethod -Method Post -Uri "$BaseUrl/produtos-servicos" -Body $prodDto -ContentType 'application/json'
$prodId = $prod.id

$servDto = @{ nome = 'Suporte'; preco = 200; tipo = 'SERVICO'; ativo = $true } | ConvertTo-Json
$serv = Invoke-RestMethod -Method Post -Uri "$BaseUrl/produtos-servicos" -Body $servDto -ContentType 'application/json'
$servId = $serv.id

$pedido = Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos"
$pedidoId = $pedido.id

Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos/$pedidoId/itens" -Body (@{ produtoServicoId = $prodId; quantidade = 1 } | ConvertTo-Json) -ContentType 'application/json'
Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos/$pedidoId/itens" -Body (@{ produtoServicoId = $servId; quantidade = 2 } | ConvertTo-Json) -ContentType 'application/json'

Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos/$pedidoId/desconto?percentual=10"

$view = Invoke-RestMethod -Method Get -Uri "$BaseUrl/pedidos/$pedidoId"
Write-Output ("pedido=$($view.id) produtos=$($view.totalProdutos) servicos=$($view.totalServicos) desconto=$($view.descontoPercentual) totalComDesconto=$($view.totalComDesconto)")