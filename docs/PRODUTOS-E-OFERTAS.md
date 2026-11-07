# Produtos, imagens e links de afiliados

## Primeiro produto

1. Entre no painel e escolha **Gerenciar produtos e ofertas → Novo produto**.
2. Informe nome, URL amigável, marca, modelo e versão exatos. Escolha a categoria na lista
   pronta. Há opções como Eletrônicos, Informática, Casa e Decoração, Moda e Beleza.
3. Informe o endereço HTTPS direto da foto que você tem autorização para usar.
   O link curto do produto, como `https://meli.la/18RdYrP`, pertence à oferta, não à imagem.
4. Salve como **Rascunho**. Você será levado à lista de ofertas do produto.
5. Clique em **Adicionar oferta / link**. Escolha Mercado Livre ou Shopee e cole seu link
   de afiliado completo, incluindo os parâmetros. O sistema não substitui sua identificação.
6. Preencha o preço conferido, escolha **Não informado**, **Frete grátis** ou **Frete com valor**,
   informe as condições e a data/hora da conferência
   no horário de Brasília. Marque **Oferta ativa** somente após conferir o anúncio.
7. Salve. Use **Conferir na loja** para verificar que o link abre o modelo/versão correto.
8. Volte a **Editar / publicar produto**, selecione **Publicado** e salve.
9. Abra a página inicial. A foto, categoria e ofertas aparecerão em um card.

Clique no nome ou na foto do card para abrir a página individual em `/produtos/{url-amigavel}`.
Ela mostra foto, descrição, marca, modelo, versão e as ofertas ativas desse produto.
As ofertas são ordenadas pelo preço do produto, sem incluir o frete. Confira as condições
e o frete em cada loja; a página não afirma que uma delas tem o menor custo total.
O botão **Ver oferta** continua abrindo diretamente o link de afiliado.

Rascunhos, arquivados e URLs inexistentes retornam página não encontrada. Um produto
publicado sem ofertas ativas mantém sua página com um aviso de indisponibilidade, sem
botões de compra. A página inicial só lista produtos com oferta ativa.

Para adicionar a outra loja, use **Adicionar oferta / link** no mesmo produto.
Somente compare modelos e versões iguais: memória, cor, tamanho, conectividade e condição
podem alterar o produto. Cadastre outro produto quando a versão for diferente.

## O que é automático e o que é manual

- O botão **Ver oferta** abre diretamente o link cadastrado, em outra aba.
- URLs e parâmetros de afiliado são preservados. O servidor não segue nem expande os links.
- O domínio precisa corresponder à loja, mas isso não comprova que o destino existe ou
  corresponde ao produto. A conferência do destino é responsabilidade do administrador.
- Links aceitos: `meli.la`, `mercadolivre.com.br` e subdomínios; `shope.ee`,
  `shopee.com.br` e subdomínios (incluindo `s.shopee.com.br`). Somente HTTPS.
- Nome, foto, preço, frete e condições não são importados do link. Não há scraping.
- Imagens são carregadas pelo navegador a partir do endereço HTTPS informado. Não há upload.
- O histórico de preço é gravado automaticamente no PostgreSQL quando o valor muda.
  Consulte-o no painel em **Gerenciar produtos e ofertas → Ofertas → Histórico de preços**.
- Não são inventadas avaliações, parcelas, preços anteriores ou descontos.

## Publicação e atualização

Um produto só pode ser publicado depois de possuir uma oferta ativa. Rascunhos e arquivados
não aparecem no catálogo; ofertas inativas também ficam ocultas. Se desativar a última
oferta ativa, o card desaparece mesmo que o produto continue com status Publicado.

Edite uma oferta para atualizar seu preço, link, condições e data da conferência. O campo
de valor aparece somente para **Frete com valor**. **Não informado** grava frete desconhecido;
**Frete grátis** grava zero e exibe esse texto no card. Informe restrições de região ou
valor mínimo nas condições de compra, quando existirem.
Uma edição desatualizada é rejeitada: reabra a página para não sobrescrever dados de outra janela.

A faixa de categorias usa as categorias reais do banco, incluindo os produtos das subcategorias.
A busca consulta nome, marca, modelo, versão e categoria no servidor, com filtro por loja,
ordenação e 12 produtos por página. Consulte `BUSCA.md`. Para organizar seleções editoriais,
use **Gerenciar rankings** no painel e consulte `RANKINGS.md`.

## Ofertas em destaque

No painel, abra as ofertas do produto e clique em **Editar oferta**. Marque
**Destacar na página inicial** e salve. A coluna **Destaque** mostra a seleção salva.
O produto precisa estar publicado e a oferta ativa para aparecer ao público.

A home sem filtros mostra até seis ofertas marcadas, ordenadas pela data da última
conferência (mais recente primeiro; em empate, maior ID primeiro). Cada card de destaque
mostra somente a oferta selecionada; a página do produto continua permitindo comparar
as demais lojas. A seção não aparece durante buscas, filtros ou nas páginas seguintes.
Sem ofertas elegíveis, a seção fica oculta.

Desmarque a opção para retirar o destaque. Desativar a oferta ou arquivar o produto também
oculta o destaque; a marcação permanece salva e volta a valer se reativar e publicar.
A seleção é editorial: não indica menor preço do mercado, desconto comprovado ou ranking.
Não há nova migração: o campo `featured` já existia na tabela de ofertas.

## Banco e testes

### Consultar o histórico

Na lista de ofertas de um produto, cada oferta tem o link **Histórico de preços**.
A tela mostra o preço inicial e as alterações, com os registros mais recentes primeiro,
25 por página. Ofertas inativas e produtos arquivados também mantêm o histórico no painel.
O acesso exige uma conta administrativa.

A data é a do registro no portal, exibida no horário de Brasília, e não a data de início
de uma promoção ou a data de conferência informada na oferta. Os valores não incluem frete.
Editar somente link, condições, frete ou status não cria um novo registro de preço.
O histórico não comprova por si só um desconto anunciado pelo marketplace.
Não é necessário cadastrar o histórico manualmente nem executar nova migração.

A migração V2 cadastra somente as lojas Mercado Livre e Shopee. Ela não cria produtos,
ofertas ou preços de exemplo e não altera contas existentes. Execute os testes de integração
somente no banco local de desenvolvimento. Os dados sintéticos dos testes são revertidos.

A migração V3 adiciona dez categorias comuns, sem alterar as já existentes. Ela evita repetir
URLs ou nomes equivalentes (ignorando acentos e maiúsculas). As categorias continuam editáveis.

Depois de atualizar o código, reinicie o site usando `scripts\iniciar.ps1` com o Docker
e o PostgreSQL em execução. A migração é aplicada automaticamente na inicialização.
