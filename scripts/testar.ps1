param([switch]$Integracao)
. "$PSScriptRoot\ambiente.ps1"
$arguments = @('-B', '-ntp', '-Dmaven.repo.local=.cache/repository', 'verify')
if ($Integracao) { $arguments += '-Pintegration' }
& .\mvnw.cmd @arguments
if ($LASTEXITCODE -ne 0) { throw 'Os testes falharam. Consulte target/surefire-reports e target/failsafe-reports.' }
