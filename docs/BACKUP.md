# Backup local do PostgreSQL

Com Docker Desktop e banco ligados, execute `scripts/testar-backup.ps1`.
Ele cria uma cópia, verifica SHA256, restaura em banco separado e verifica as tabelas
e o trigger do histórico. Ao terminar, remove apenas o banco temporário criado pelo teste.
O arquivo de backup permanece guardado. O banco usado pelo site não é substituído.

## Criar uma cópia

Execute `scripts/backup.ps1`. Os arquivos `.dump` e `.dump.sha256` ficam em `backups/`,
com data, hora e identificador único. A pasta é ignorada pelo Git.
O pg_dump usa formato custom e o Docker copia o binário sem redirecionamento de bytes
pelo PowerShell. A cópia inclui tabelas, dados, sequências e funções do banco da aplicação.
Não inclui arquivos do projeto, imagens externas, configurações do Docker, `.env` ou papéis
globais do PostgreSQL. Guarde o código e os segredos necessários em local protegido separado.

## Recuperar sem substituir o original

```powershell
.\scripts\restaurar-backup.ps1 -Arquivo 'CAMINHO-COMPLETO-DO-BACKUP.dump'
```

O script exige o `.sha256` ao lado do arquivo e cria um banco novo chamado
`td_recuperacao_...`. Sem `-ValidarEExcluir`, a restauração bem-sucedida permanece disponível.
A aplicação não muda de banco automaticamente. Usar o banco recuperado deve ser uma
operação planejada, incluindo credenciais, conferência dos dados e configuração `DB_URL`.
Use somente backups confiáveis: restaurar SQL executa funções e comandos do arquivo.

## Cuidados operacionais

- Copie `.dump` e `.sha256` juntos para outro disco ou armazenamento protegido. Uma cópia
  no mesmo computador não protege contra perda do disco.
- O backup contém e-mails administrativos e hashes de senha: restrinja acesso. Ele não
  é criptografado; SHA256 verifica alteração acidental, não garante autenticidade.
- Não há exclusão automática nem agendamento nesta etapa. Acompanhe o espaço disponível.
- Restauração testada por estrutura não substitui revisão funcional dos dados recuperados.
- Os scripts atuais atendem ao banco local `tabaratodemais` no serviço Compose `db`, com
  ferramentas PostgreSQL 17 do próprio container.

## Validação desta entrega

Sintaxe dos scripts verificada no PowerShell. A execução real de pg_dump/pg_restore ficou
pendente: o ambiente do agente negou acesso à API Docker. Execute `testar-backup.ps1`
no PowerShell do proprietário para concluir a verificação antes de confiar na cópia.
