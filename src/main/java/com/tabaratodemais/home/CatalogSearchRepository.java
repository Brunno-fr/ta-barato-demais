package com.tabaratodemais.home;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogSearchRepository {
    public static final int PAGE_SIZE = 12;
    private final NamedParameterJdbcTemplate jdbc;
    public CatalogSearchRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Result(List<Long> ids, long total, int page, int pages) {}

    public Result search(CatalogFilter filter) {
        var parameters = new MapSqlParameterSource();
        String cte = "";
        if (filter.category() != null) {
            cte = "WITH RECURSIVE selected_categories(id) AS (SELECT id FROM categories WHERE id=:category "
                + "UNION SELECT c.id FROM categories c JOIN selected_categories sc ON c.parent_id=sc.id) ";
            parameters.addValue("category", filter.category());
        }
        String storePredicate = "";
        if (!filter.store().isEmpty()) {
            storePredicate = " AND s.slug=:store";
            parameters.addValue("store", filter.store());
        }
        // Agrupar antes de paginar evita repetir produtos com ofertas nas duas lojas.
        String from = " FROM products p JOIN categories c ON c.id=p.category_id JOIN "
            + "(SELECT o.product_id, min(o.price) AS min_price FROM offers o JOIN stores s ON s.id=o.store_id "
            + "WHERE o.active=true" + storePredicate + " GROUP BY o.product_id) prices ON prices.product_id=p.id "
            + "WHERE p.status='PUBLISHED'";
        if (filter.category() != null) from += " AND p.category_id IN (SELECT id FROM selected_categories)";
        String query = Normalizer.normalize(filter.query(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        int index = 0;
        for (String term : query.split("\\s+")) {
            if (term.isBlank()) continue;
            String key = "term" + index++;
            from += " AND strpos(translate(lower(concat_ws(' ',p.name,p.brand,p.model,p.variant,c.name)),"
                + "'áàâãäéèêëíìîïóòôõöúùûüç','aaaaaeeeeiiiiooooouuuuc'),:" + key + ") > 0";
            parameters.addValue(key, term);
        }
        long total = jdbc.queryForObject(cte + "SELECT count(*)" + from, parameters, Long.class);
        int pages = (int) Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(filter.page(), pages);
        String order = switch (filter.sort()) {
            case "preco-desc" -> "prices.min_price DESC, p.id ASC";
            case "recentes" -> "p.created_at DESC, p.id DESC";
            default -> "prices.min_price ASC, p.id ASC";
        };
        parameters.addValue("limit", PAGE_SIZE).addValue("offset", (long) (page - 1) * PAGE_SIZE);
        List<Long> ids = total == 0 ? List.of() : jdbc.queryForList(cte + "SELECT p.id" + from
            + " ORDER BY " + order + " LIMIT :limit OFFSET :offset", parameters, Long.class);
        return new Result(ids, total, page, pages);
    }
}
