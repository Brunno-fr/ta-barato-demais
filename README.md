# Tá Barato Demais

Aplicação web para encontrar e comparar ofertas de produtos. O catálogo é mantido manualmente por um administrador; os preços e links exibidos são os cadastrados no sistema.

## Funcionalidades

- Catálogo público com busca, filtros por categoria e loja, ordenação e paginação.
- Página de produto com comparação de ofertas ativas do Mercado Livre e da Shopee.
- Painel administrativo para gerenciar produtos, categorias, ofertas e rankings.
- Histórico de preços registrado no banco de dados.
- Autenticação e autorização administrativas com Spring Security, BCrypt e proteção CSRF.
- Metadados de SEO, sitemap e páginas administrativas protegidas.

## Tecnologias

Java 21 · Spring Boot · Spring MVC · Spring Data JPA · Spring Security · Thymeleaf · PostgreSQL 17 · Flyway · Maven

## Executar localmente no Windows

### Pré-requisitos

- JDK 21
- Docker Desktop com Docker Compose
- PowerShell

Na pasta do projeto, inicie o banco de dados:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\banco.ps1
```

Depois inicie a aplicação:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\iniciar.ps1
```

Abra [http://localhost:8080](http://localhost:8080). O banco fica disponível localmente na porta `5433`.

Na primeira execução, o script do banco cria um arquivo `.env` com uma senha aleatória para o PostgreSQL. Mantenha esse arquivo local e não o envie ao Git. Para parar o banco sem apagar os dados, execute:

```powershell
docker compose stop
```

### Criar o primeiro administrador

Não há conta administrativa padrão. Crie a primeira conta localmente com:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\criar-admin.ps1
```

As instruções aparecem no terminal; a senha é digitada de forma oculta. A administração fica em `/admin`.

## Testes

Para executar os testes sem banco:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\testar.ps1
```

Para incluir os testes de integração, inicie o PostgreSQL e use:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\testar.ps1 -Integracao
```

Os testes de integração usam o banco de desenvolvimento configurado e aplicam migrações pendentes.

## Escopo atual

Produtos, categorias e ofertas são cadastrados manualmente no painel. A aplicação não importa produtos automaticamente nem coleta preços das lojas. Rankings e destaques dependem da seleção administrativa, e a indexação por mecanismos de busca permanece desativada durante a preparação do projeto.

## Documentação

- [Administração](docs/ADMINISTRACAO.md)
- [Produtos e ofertas](docs/PRODUTOS-E-OFERTAS.md)
- [Categorias](docs/CATEGORIAS.md)
- [Busca](docs/BUSCA.md)
- [Rankings](docs/RANKINGS.md)
- [SEO](docs/SEO.md)
- [Backups](docs/BACKUP.md)
- [Plano do projeto](docs/PLANO.md)
