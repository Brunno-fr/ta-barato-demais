# Acesso administrativo

## Criar o primeiro administrador

Mantenha o PostgreSQL iniciado pelo Docker. Na pasta do projeto, execute em um
PowerShell normal, interativo, sem redirecionar a entrada ou saída:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\criar-admin.ps1
```

O comando compila o projeto, executa os testes sem banco e abre o assistente no terminal.
Informe seu e-mail, uma senha de pelo menos 12 caracteres e a confirmação. A senha não
aparece na tela. Use uma frase longa e exclusiva; BCrypt aceita até 72 bytes UTF-8
(caracteres acentuados podem ocupar mais de um byte).

Não envie a senha no chat, não a inclua na linha de comando e não a grave no `.env`.
O banco recebe somente o hash BCrypt. Nenhuma senha padrão é criada.
O comando não abre um servidor HTTP e se encerra ao terminar. Se já existir uma conta,
ele recusa a operação sem alterar credenciais. Uma trava transacional no PostgreSQL
também impede duas execuções simultâneas de criarem o primeiro administrador.

## Entrar e sair

Reinicie o Spring Boot caso estivesse executando uma versão anterior: Ctrl+C no terminal
da aplicação, depois execute `scripts/iniciar.ps1` novamente.

Abra http://localhost:8080/admin/login e use o e-mail e a senha cadastrados.
O painel mostra a conta autenticada. O botão **Sair da conta** encerra a sessão.
A página pública continua em http://localhost:8080.

## Comportamento verificado

- Visitantes sem sessão são redirecionados de `/admin` ao login.
- Apenas a autoridade `ROLE_ADMIN` permite acessar o painel.
- E-mail é normalizado; credenciais inexistentes, incorretas ou contas desativadas
  recebem a mesma mensagem genérica de falha.
- Login e logout exigem token CSRF. Thymeleaf o inclui nos formulários POST.
- Spring Security renova o identificador de sessão no login para evitar fixação de sessão.
- Logout invalida a sessão e remove o cookie `JSESSIONID`.
- A sessão expira após 30 minutos de inatividade; cookie HttpOnly e SameSite=Lax.

## Limites desta etapa

Ainda não há recuperação de senha, cadastro público, gestão de contas, MFA ou CRUD de categorias.
O assistente serve somente para a criação inicial; ele não é um comando de redefinição de senha.
A aplicação continua vinculada ao localhost. Antes da exposição pública, configurar HTTPS,
cookie Secure, limitação de tentativas de login, monitoramento e recuperação de acesso.

Os testes de integração usam contas sintéticas em transações com rollback, sem deixar
usuários de teste cadastrados. O assistente interativo deve ser executado pelo proprietário
para escolher suas credenciais; o agente não cria uma conta permanente por conta própria.
