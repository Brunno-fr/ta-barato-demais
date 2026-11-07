# Busca, filtros e páginas

A pesquisa da página inicial agora consulta o banco, em vez de esconder cards já carregados.
São exibidos até **12 produtos por página**, somente publicados e com ofertas ativas.

- Use a busca para procurar nome, marca, modelo, versão ou nome da categoria.
  Maiúsculas e acentos em português não impedem a correspondência. Todas as palavras
  digitadas precisam estar presentes nos campos pesquisados.
- Clique em uma categoria para incluir seus produtos e os das suas subcategorias.
- Escolha **Mercado Livre**, **Shopee** ou **Todas as lojas** e clique em **Aplicar filtros**.
- Ordene por menor preço, maior preço ou cadastros mais recentes.
- Use **Anterior** e **Próxima** quando houver mais de 12 produtos.
- Use **Limpar filtros** para voltar ao catálogo completo.

A ordenação de preço usa a menor oferta ativa de cada produto nas lojas selecionadas,
sem incluir frete. Ela não afirma qual produto tem o menor custo total. Quando uma loja
é selecionada, somente suas ofertas aparecem nos cards. A página individual continua
mostrando as ofertas ativas de todas as lojas para comparação.

O endereço preserva a consulta, categoria, loja, ordenação e página. Exemplo:
`/?q=smartphone&loja=shopee&ordem=preco-asc&pagina=1#destaques`.
Copie a URL para repetir a pesquisa. Endereços com `localhost` funcionam somente no
computador que está executando o site; compartilhamento público depende da publicação.

Alterar busca, categoria ou loja reinicia na primeira página, mantendo os demais filtros.
Uma página maior que a última disponível é ajustada para a última página. Valores inválidos
de loja, ordenação, categoria ou página retornam HTTP 400. A consulta aceita até 100 caracteres.

## Implementação

`CatalogFilter` valida parâmetros e monta URLs. `CatalogSearchRepository` faz contagem,
filtro e paginação no PostgreSQL com parâmetros vinculados. A seleção de categoria percorre
descendentes por consulta recursiva. Produtos são agrupados antes da paginação, evitando
duplicação quando possuem várias ofertas. `CatalogService` carrega as ofertas somente
dos produtos da página selecionada.

Formulários e links usam GET e funcionam sem JavaScript. Não há nova migração de banco.
Os testes cobrem categorias descendentes, loja, ordenação, paginação estável, acentos,
termos literais, parâmetros inválidos, preservação dos filtros e produtos não publicados.
