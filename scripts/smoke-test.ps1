param(
  [string]$EnvFile = ".env",
  [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "[SMOKE] Carregando variáveis de $EnvFile" -ForegroundColor Cyan
Get-Content $EnvFile | ForEach-Object {
  $kv = $_ -split '=',2
  if ($kv.Length -eq 2) { Set-Item -Path Env:\$($kv[0]) -Value $kv[1] }
}

Write-Host "[SMOKE] Build do jar" -ForegroundColor Cyan
mvn -q -DskipTests package

$jar = Join-Path $PSScriptRoot "..\target\desafioII-0.0.1-SNAPSHOT.jar"
if (!(Test-Path $jar)) { throw "Jar não encontrado em $jar" }

Write-Host "[SMOKE] Iniciando aplicação" -ForegroundColor Cyan
$proc = Start-Process -FilePath "java" -ArgumentList "-jar `"$jar`"" -PassThru -NoNewWindow

try {
  $healthUrl = "$BaseUrl/actuator/health"
  $deadline = (Get-Date).AddMinutes(2)
  do {
    Start-Sleep -Seconds 2
    try {
      $health = Invoke-RestMethod -Method Get -Uri $healthUrl -TimeoutSec 5
      if ($health.status -eq "UP") { break }
    } catch {}
  } while ((Get-Date) -lt $deadline)

  Write-Host "[SMOKE] Criando catálogo" -ForegroundColor Cyan
  $prodDto = @{ nome = 'Notebook'; preco = 3000; tipo = 'PRODUTO'; ativo = $true } | ConvertTo-Json
  $prod = Invoke-RestMethod -Method Post -Uri "$BaseUrl/produtos-servicos" -Body $prodDto -ContentType 'application/json'
  $servDto = @{ nome = 'Suporte'; preco = 200; tipo = 'SERVICO'; ativo = $true } | ConvertTo-Json
  $serv = Invoke-RestMethod -Method Post -Uri "$BaseUrl/produtos-servicos" -Body $servDto -ContentType 'application/json'

  Write-Host "[SMOKE] Criando pedido e itens" -ForegroundColor Cyan
  $pedido = Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos"
  Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos/$($pedido.id)/itens" -Body (@{ produtoServicoId = $prod.id; quantidade = 1 } | ConvertTo-Json) -ContentType 'application/json'
  Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos/$($pedido.id)/itens" -Body (@{ produtoServicoId = $serv.id; quantidade = 2 } | ConvertTo-Json) -ContentType 'application/json'

  Write-Host "[SMOKE] Aplicando desconto" -ForegroundColor Cyan
  Invoke-RestMethod -Method Post -Uri "$BaseUrl/pedidos/$($pedido.id)/desconto?percentual=10"

  $view = Invoke-RestMethod -Method Get -Uri "$BaseUrl/pedidos/$($pedido.id)"
  Write-Host ("[RESULT] Pedido=$($view.id) Produtos=$($view.totalProdutos) Servicos=$($view.totalServicos) Desconto=$($view.descontoPercentual) TotalComDesconto=$($view.totalComDesconto)") -ForegroundColor Green
} finally {
  if ($proc -and !$proc.HasExited) { Write-Host "[SMOKE] Encerrando aplicação" -ForegroundColor Cyan; $proc.Kill() }
}