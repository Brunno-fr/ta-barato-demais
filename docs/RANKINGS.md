# Rankings editoriais

## Cadastrar e publicar

1. No painel, abra **Gerenciar rankings → Novo ranking**.
2. Informe título, URL amigável (ex.: `fones-para-estudar`), descrição e os critérios
   da seleção. Registre as fontes consultadas e limitações; não invente avaliações.
3. Escolha até dez produtos cadastrados na lista de cada posição e explique o motivo
   de cada indicação. Cada produto só pode aparecer uma vez. Posições vazias são ignoradas.
4. Para alterar a ordem, troque os produtos e suas justificativas entre as posições.
5. Deixe **Publicar ranking** desmarcado para salvar como rascunho. A publicação exige
   pelo menos dois produtos e todos os selecionados devem estar publicados.
6. Marque **Publicar ranking** e salve. Use **Ver página pública** na listagem.

A página fica em `/rankings/{url-amigavel}`. O menu da home leva à lista `/rankings`.
Desmarcar a publicação retira a página do ar; rascunhos e URLs inexistentes retornam 404.
Edições antigas são rejeitadas para não sobrescrever alterações de outra janela.

## Regras públicas

- Posições são decisões editoriais, não notas de compradores nem ordenação automática por preço.
- Produtos arquivados ou em rascunho ficam ocultos. As posições dos demais não são alteradas.
- Se todas as entradas forem ocultadas, a página informa a indisponibilidade da seleção.
- Produtos publicados sem ofertas ativas continuam visíveis com aviso, sem botão de compra.
- Os preços, frete, condições e datas vêm das ofertas reais cadastradas. Os links de
  afiliado são preservados. A data da seleção é separada da conferência das ofertas.
- A página informa a possibilidade de comissão. Descrições e justificativas são texto
  simples, escapado pelo Thymeleaf; não aceitam HTML executável.

## Implementação

V4 cria `rankings` e `ranking_items`, sem criar listas ou produtos de exemplo.
Há chaves estrangeiras e restrições para evitar produtos e posições repetidos.
O cadastro usa a autenticação administrativa e CSRF existentes. A gravação é transacional,
com controle de versão. As consultas editoriais usam JDBC parametrizado e reutilizam
o catálogo JPA para os produtos e ofertas, sem importar dados dos marketplaces.

Limites desta versão: dez posições por ranking; ordenação manual por posição;
sem upload, notas, geração automática de conteúdo ou editor de texto rico.
As listas de rankings e o seletor de produtos ainda não têm paginação.
