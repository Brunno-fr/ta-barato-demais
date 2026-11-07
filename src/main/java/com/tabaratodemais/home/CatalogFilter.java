package com.tabaratodemais.home;

import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record CatalogFilter(String query, Long category, String store, String sort, int page) {
    public String url(Long selectedCategory, int selectedPage) {
        var uri=org.springframework.web.util.UriComponentsBuilder.fromPath("/");
        if(!query.isEmpty()) uri.queryParam("q","{query}");
        if(selectedCategory!=null) uri.queryParam("categoria",selectedCategory);
        if(!store.isEmpty()) uri.queryParam("loja",store);
        uri.queryParam("ordem",sort).queryParam("pagina",selectedPage).fragment("destaques");
        return uri.encode().buildAndExpand(java.util.Map.of("query",query)).toUriString();
    }
    public CatalogFilter {
        query = query == null ? "" : query.strip();
        store = store == null ? "" : store;
        sort = sort == null ? "preco-asc" : sort;
        if (query.length() > 100 || category != null && category < 1 || page < 1
                || !Set.of("", "mercado-livre", "shopee").contains(store)
                || !Set.of("preco-asc", "preco-desc", "recentes").contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtros inválidos.");
        }
    }
}
