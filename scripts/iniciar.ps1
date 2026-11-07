. "$PSScriptRoot\ambiente.ps1"
if (-not $env:DB_PASSWORD) { throw 'Execute primeiro scripts\banco.ps1.' }
& .\mvnw.cmd -B -ntp '-Dmaven.repo.local=.cache/repository' spring-boot:run
if ($LASTEXITCODE -ne 0) { throw 'A aplicação não iniciou; consulte a mensagem acima.' }
