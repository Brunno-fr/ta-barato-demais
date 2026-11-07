# SEO e compartilhamento

## Estado atual: desenvolvimento

A indexação continua desligada por padrão (`SITE_INDEXING_ENABLED=false`).
As páginas públicas recebem `noindex, nofollow` tanto no HTML quanto no cabeçalho
`X-Robots-Tag`; `robots.txt` solicita que os robôs não rastreiem o site.
Isso não substitui autenticação nem impede acesso humano. A aplicação continua no localhost.

O endereço padrão é `http://localhost:8080`. Não foi registrado domínio, publicada a
aplicação ou enviado sitemap ao Google. As prévias de compartilhamento só poderão ser
buscadas pelas redes sociais quando a página e suas imagens estiverem acessíveis na internet.

## Implementado

- Títulos e descrições específicos para início, produtos, lista de rankings e cada ranking.
- Canonical absoluto montado com `SITE_BASE_URL`, sem confiar no cabeçalho Host recebido.
- Open Graph e Twitter Cards com título, descrição, URL e imagem. Produtos usam sua foto;
  outras páginas usam a marca já existente. Descrições limitadas a 160 caracteres.
- `/sitemap.xml` é um índice; `/sitemaps/1.xml` e seguintes têm até 1.000 URLs cada.
  Entram home, lista de rankings, produtos publicados e rankings publicados. Rascunhos,
  arquivados, administração e URLs com filtros não entram. Não são inventadas datas de
  alteração, prioridades, avaliações ou descontos.
- Buscas e páginas com parâmetros permanecem `noindex`, mesmo após liberar indexação.
  Seus canonicals apontam à rota sem parâmetros. Administração e respostas de erro também
  recebem `X-Robots-Tag: noindex, nofollow`.

## Configuração futura no servidor de produção

Configure no ambiente da hospedagem, somente depois de confirmar domínio e HTTPS:

```text
SITE_BASE_URL=https://SEU-DOMINIO-CONFIRMADO
SITE_INDEXING_ENABLED=true
```

`SITE_BASE_URL` deve ser somente a origem, sem caminho, credenciais, consulta ou fragmento.
A liberação exige HTTPS e rejeita localhost. Reinicie a aplicação após alterar as variáveis.
O script local `ambiente.ps1` lê apenas a senha do `.env`; as variáveis de SEO devem ser
definidas no ambiente do processo, não adicionadas ao `.env` esperando importação automática.
Se usar outra porta local, ajuste `SITE_BASE_URL` para corresponder ao endereço de acesso.

Quando a indexação estiver habilitada, `robots.txt` permite o rastreamento público,
restringe `/admin` e informa o sitemap com o domínio configurado. A configuração sozinha
não garante indexação nem posição no Google. Um ambiente de homologação público deve
ter controle de acesso: bloquear rastreamento pode impedir que robôs leiam a regra noindex.

Referências: [Google — noindex](https://developers.google.com/search/docs/crawling-indexing/block-indexing)
e [Open Graph](https://ogp.me/).

## Conferência local

Após reiniciar, abra `/robots.txt`, `/sitemap.xml` e `/sitemaps/1.xml`.
Os metadados ficam no código-fonte das páginas; o layout visual não muda.
O sitemap local serve para conferência, mesmo com indexação desligada.
