# Categorias e subcategorias

Com o banco e o site em execução, entre em `/admin` e clique em **Gerenciar categorias**.

A aplicação já inclui uma lista inicial: Eletrônicos, Informática, Eletrodomésticos,
Casa e Decoração, Beleza e Cuidados, Moda, Esportes e Lazer, Ferramentas, Brinquedos e
Automotivo. Categorias equivalentes já cadastradas são preservadas. No cadastro de produto,
basta selecionar uma opção; subcategorias mostram também o nome da categoria principal.

Na página pública, categorias conhecidas recebem ícones específicos (carro, computador,
camiseta etc.), reconhecidos pelo nome ou URL amigável. Nomes sem acentos também funcionam.
Categorias personalizadas usam o ícone da principal quando reconhecida; caso contrário,
usam a grade genérica. Essa apresentação não altera os cadastros nem os filtros.

1. Clique em **Nova categoria**.
2. Informe o nome, por exemplo `Eletrônicos`, e a URL amigável `eletronicos`.
3. Deixe a categoria principal vazia e salve.
4. Para uma subcategoria, cadastre por exemplo `Fones de ouvido`, URL `fones-de-ouvido`,
   e selecione `Eletrônicos` como categoria principal.
5. Use **Editar** na listagem para alterar nome, URL ou categoria principal.

Os exemplos não são inseridos automaticamente. Os dados são gravados no PostgreSQL
existente. Não há exclusão nesta etapa para evitar remover relações usadas por futuros produtos.
O catálogo público usa essas categorias para filtrar os produtos publicados com ofertas ativas.

## Regras

- Nome obrigatório, com até 100 caracteres.
- URL obrigatória e única, até 140 caracteres: letras minúsculas sem acentos, números e
  hífens entre palavras. A URL é um identificador, não um endereço completo.
- Uma categoria não pode ser sua própria principal nem pertencer a uma de suas descendentes.
- A categoria principal pode ser removida na edição para transformar a subcategoria em raiz.
- Apenas administradores acessam as telas; formulários exigem token CSRF.

## Implementação e testes

`CategoryAdminController` recebe e valida o formulário, `CategoryService` aplica as regras,
e `CategoryRepository` acessa o banco. A constraint única existente protege a URL.
O bloqueio transacional do PostgreSQL serializa alterações da árvore para evitar ciclos
quando dois administradores editam simultaneamente. Não foi necessária nova migração.

Os testes de interface verificam autorização, CSRF, escape de HTML, validação e formulários.
Os testes de integração verificam gravação/edição no PostgreSQL, URL duplicada e ciclos.
Os dados de teste são revertidos ao final de cada teste, preservando os cadastros reais.

Execute `scripts\testar.ps1 -Integracao` com o PostgreSQL disponível.
