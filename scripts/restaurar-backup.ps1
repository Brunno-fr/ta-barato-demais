param(
    [Parameter(Mandatory=$true)][string]$Arquivo,
    [switch]$ValidarEExcluir
)
$ErrorActionPreference = 'Stop'
# Resolve antes de ambiente.ps1 mudar a pasta atual.
$source = (Resolve-Path -LiteralPath $Arquivo).Path
. "$PSScriptRoot\ambiente.ps1"
if (-not (Test-Path -LiteralPath "$source.sha256")) { throw 'Arquivo .sha256 ausente. Mantenha-o ao lado do backup.' }
$expected = (Get-Content -LiteralPath "$source.sha256" -Raw).Trim()
if ($expected -notmatch '^[A-Fa-f0-9]{64}$' -or (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne $expected) {
    throw 'A verificacao SHA256 falhou. O backup pode estar incompleto ou alterado.'
}
$token = [guid]::NewGuid().ToString('N')
$database = "td_recuperacao_$token"
$remote = "/tmp/td-recuperacao-$token.dump"
$created = $false
$success = $false
try {
    docker compose cp $source "db:$remote"
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao copiar backup para o container.' }
    docker compose exec -T db createdb -U tabaratodemais --template=template0 $database
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar banco separado para restauracao.' }
    $created = $true
    docker compose exec -T db pg_restore -U tabaratodemais -d $database --exit-on-error --single-transaction --no-owner --no-acl $remote
    if ($LASTEXITCODE -ne 0) { throw 'Falha na restauracao. O banco original nao foi alterado.' }
    $sql = @'
DO $$
DECLARE t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['users','categories','products','stores','offers','price_history','rankings','ranking_items','flyway_schema_history'] LOOP
    IF to_regclass('public.' || t) IS NULL THEN RAISE EXCEPTION 'Tabela ausente: %', t; END IF;
  END LOOP;
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname='offer_price_history' AND NOT tgisinternal) THEN
    RAISE EXCEPTION 'Trigger do historico ausente';
  END IF;
END $$;
SELECT 'users' AS tabela,count(*) AS registros FROM users
UNION ALL SELECT 'products',count(*) FROM products
UNION ALL SELECT 'offers',count(*) FROM offers
UNION ALL SELECT 'price_history',count(*) FROM price_history
UNION ALL SELECT 'rankings',count(*) FROM rankings;
'@
    $sql | docker compose exec -T db psql -U tabaratodemais -d $database -v ON_ERROR_STOP=1
    if ($LASTEXITCODE -ne 0) { throw 'Restauracao concluida, mas a verificacao da estrutura falhou.' }
    $success = $true
    if ($ValidarEExcluir) { Write-Host 'Backup restaurado e estrutura verificada. O banco temporario sera removido.' }
    else { Write-Host "Banco recuperado: $database"; Write-Host 'O site continua usando o banco original. A troca exige uma etapa separada.' }
} finally {
    if ($created -and ($ValidarEExcluir -or -not $success)) {
        # Exclui somente o banco unico que este script acabou de criar.
        if ($database -notmatch '^td_recuperacao_[a-f0-9]{32}$') { throw 'Nome inesperado; limpeza cancelada.' }
        docker compose exec -T db dropdb -U tabaratodemais $database
        if ($LASTEXITCODE -ne 0) { Write-Warning "Nao foi possivel remover o banco temporario $database." }
    }
    docker compose exec -T db rm -f -- $remote 2>$null | Out-Null
}
