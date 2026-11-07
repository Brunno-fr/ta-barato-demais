# Inicia o PostgreSQL e aguarda ficar pronto antes de criar a conta.
. "$PSScriptRoot\banco.ps1"
# Monta um executável atualizado. A senha só será solicitada pelo console Java.
& .\mvnw.cmd -B -ntp '-Dmaven.repo.local=.cache/repository' package
if ($LASTEXITCODE -ne 0) { throw 'Compilacao ou testes falharam. Nenhuma conta foi criada.' }
& "$env:JAVA_HOME\bin\java.exe" -jar target\tabaratodemais-0.0.1-SNAPSHOT.jar --setup-admin
if ($LASTEXITCODE -ne 0) { throw 'O administrador nao foi criado. Consulte a mensagem acima.' }
