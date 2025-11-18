$base = "http://localhost:8080"

$prodBody = @{ nome = 'Notebook'; preco = 3000; tipo = 'PRODUTO'; ativo = $true } | ConvertTo-Json
$prod = Invoke-RestMethod -Method Post -Uri "$base/produtos-servicos" -ContentType 'application/json' -Body $prodBody

$servBody = @{ nome = 'Suporte'; preco = 200; tipo = 'SERVICO'; ativo = $true } | ConvertTo-Json
$serv = Invoke-RestMethod -Method Post -Uri "$base/produtos-servicos" -ContentType 'application/json' -Body $servBody

$pedido = Invoke-RestMethod -Method Post -Uri "$base/pedidos"

$add1Body = @{ produtoServicoId = $prod.id; quantidade = 1 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/pedidos/$($pedido.id)/itens" -ContentType 'application/json' -Body $add1Body | Out-Null

$add2Body = @{ produtoServicoId = $serv.id; quantidade = 2 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/pedidos/$($pedido.id)/itens" -ContentType 'application/json' -Body $add2Body | Out-Null

Invoke-RestMethod -Method Post -Uri "$base/pedidos/$($pedido.id)/desconto?percentual=10" | Out-Null

$view = Invoke-RestMethod -Method Get -Uri "$base/pedidos/$($pedido.id)"

$result = [PSCustomObject]@{
  id = $view.id
  situacao = $view.situacao
  descontoPercentual = $view.descontoPercentual
  totalProdutos = $view.totalProdutos
  totalServicos = $view.totalServicos
  totalComDesconto = $view.totalComDesconto
  itens = $view.itens
}

$outPath = Join-Path (Resolve-Path "..") "curls para teste\example_output.json"
$result | ConvertTo-Json -Depth 8 | Out-File -FilePath $outPath -Encoding utf8
Write-Output $outPath