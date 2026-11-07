$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\ambiente.ps1"
$backupDir = Join-Path (Get-Location) 'backups'
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
$token = (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0,8)
$remote = "/tmp/tabaratodemais-$token.dump"
$destination = Join-Path $backupDir "tabaratodemais-$token.dump"
try {
    # O arquivo binario e copiado pelo Docker, sem redirecionamento do PowerShell.
    docker compose exec -T db pg_dump -U tabaratodemais -d tabaratodemais --format=custom --no-owner --no-acl --file=$remote
    if ($LASTEXITCODE -ne 0) { throw 'Falha no pg_dump. Confira se Docker e banco estao ligados.' }
    docker compose exec -T db pg_restore --list $remote | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'O arquivo gerado nao passou na leitura do pg_restore.' }
    docker compose cp "db:$remote" $destination
    if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel copiar o backup para o computador.' }
    if ((Get-Item -LiteralPath $destination).Length -eq 0) { throw 'Backup vazio.' }
    $hash = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash
    Set-Content -LiteralPath "$destination.sha256" -Value $hash -Encoding ascii
    Write-Host "Backup criado: $destination"
    Write-Host 'Guarde o arquivo .dump e seu .sha256 juntos em outro disco ou armazenamento protegido.'
    Write-Output $destination
} finally {
    # Caminho gerado internamente, restrito ao arquivo temporario desta execucao.
    docker compose exec -T db rm -f -- $remote 2>$null | Out-Null
}
