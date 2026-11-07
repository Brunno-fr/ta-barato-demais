# Plano do Tabarato Demais

## Direção visual definida pelo proprietário

Referência aprovada em 30/10/2026: fundo preto, verde-limão, logo metálico,
cabeçalho compacto, banner, busca, categorias em faixa e seis cards em telas largas.
Logo original fornecido pelo proprietário: `src/main/resources/static/images/brand.png`.
Referência fornecida: `src/main/resources/static/images/visual-reference.png`.

A imagem de referência é usada temporariamente como fundo em janelas CSS para o banner,
marcas e ilustrações. Ela não substitui o HTML da página. Antes do lançamento, substituir
os assets das marcas por arquivos autorizados. Os produtos agora usam a URL de imagem de cada cadastro.
Não há preços, descontos, avaliações ou links comerciais fictícios no HTML.
A busca e os filtros consultam o PostgreSQL, sem consulta automática aos marketplaces.

## Sequência de desenvolvimento

1. **Fundação concluída:** Spring Boot, PostgreSQL, Flyway, entidades, home e testes.
2. **Direção visual aplicada:** logo, cores, banner e organização conforme referência.
   A conferência visual final em desktop e celular depende de abrir a versão atualizada.
3. **Administração segura implementada:** criação controlada do primeiro administrador,
   BCrypt, login, logout, autorização de `/admin` e testes de acesso.
   Criação da conta e login confirmados pelo proprietário em 30/10/2026.
4. **Categorias implementadas:** listagem, cadastro, edição e hierarquia com prevenção de
   ciclos, URL amigável única, autorização administrativa e proteção CSRF.
5. **Produtos implementados:** cadastro/edição, modelo/variante, imagem por URL HTTPS,
   publicação e arquivamento. Upload de arquivos ainda não disponível.
6. **Lojas e ofertas implementadas:** Mercado Livre e Shopee, links de afiliados com
   domínio validado, preços manuais, condições, frete e data de conferência. Histórico
   registrado no banco e disponível para consulta por oferta no painel, com paginação.
7. **Catálogo público integrado:** cards reais com ofertas agrupadas por produto e links
   diretos e página individual com descrição, versão e comparação das ofertas ativas.
   Busca, categorias e loja filtram no servidor, com ordenação, URLs compartilháveis
   e paginação de 12 produtos. Não há carregamento de todo o catálogo no navegador.
8. **Destaques implementados:** seleção manual por oferta no painel. A home exibe até seis
   ofertas ativas de produtos publicados, por conferência mais recente.
   **Rankings implementados:** cadastro editorial com critérios, até dez posições e
   justificativas, rascunho/publicação, página própria e listagem pública. Consulte `RANKINGS.md`.
9. **Preparação para lançamento:** SEO básico implementado (metadados, canonical,
   compartilhamento, robots e sitemap paginado). Indexação continua desabilitada.
   Domínio, hospedagem, HTTPS, backups, monitoramento e revisão final permanecem pendentes.
   Consulte `SEO.md` antes de configurar o ambiente público.

Cada etapa deve passar nos testes antes de avançar. Refinamentos visuais ficam restritos
ao necessário para integrar a funcionalidade da etapa, evitando redesenhos contínuos.
