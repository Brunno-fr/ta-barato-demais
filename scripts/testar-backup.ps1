$ErrorActionPreference = 'Stop'
$arquivo = & "$PSScriptRoot\backup.ps1"
if (-not $arquivo) { throw 'Backup nao gerado; restauracao cancelada.' }
& "$PSScriptRoot\restaurar-backup.ps1" -Arquivo $arquivo -ValidarEExcluir
Write-Host 'Teste de backup concluido. A copia foi preservada na pasta backups.'
