param(
  [string]$DbUrl,
  [string]$DbUser,
  [string]$DbPass,
  [int]$Port = 8080
)

$env:DB_URL = $DbUrl
$env:DB_USER = $DbUser
$env:DB_PASSWORD = $DbPass

$root = Resolve-Path (Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "..")

Start-Process -FilePath "java" -ArgumentList "-jar","target\desafioII-0.0.1-SNAPSHOT.jar" -WorkingDirectory $root -WindowStyle Minimized | Out-Null
Start-Sleep -Seconds 12

$base = "http://localhost:$Port"

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

$out = [PSCustomObject]@{
  pedido = $pedido.id
  produto = $prod.id
  servico = $serv.id
  totalComDesconto = $view.totalComDesconto
  totalProdutos = $view.totalProdutos
  totalServicos = $view.totalServicos
}

$outPath = Join-Path (Resolve-Path "..") "curls para teste\smoke_output.json"
$out | ConvertTo-Json -Depth 6 | Out-File -FilePath $outPath -Encoding utf8
Write-Output $outPath