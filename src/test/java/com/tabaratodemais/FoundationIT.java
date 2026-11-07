package com.tabaratodemais;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.offer.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

// PostgreSQL real, com rollback de todos os dados sintéticos ao final de cada teste.
@SpringBootTest
@Transactional
class FoundationIT {
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProductRepository products;

    @Test void migrationsMappingsRelationshipsAndPriceHistoryWorkTogether() {
        var category = new Category("Teste automatizado", "test-" + java.util.UUID.randomUUID(), null);
        em.persist(category);
        var product = products.save(new Product(category, "Produto sintético", "test-" + java.util.UUID.randomUUID(), "Teste", "M1", "V1"));
        var store = new Store("Loja sintética", "test-" + java.util.UUID.randomUUID());
        em.persist(store);
        var offer = new Offer(product, store, new BigDecimal("100.00"), "https://example.com/test", Instant.now());
        em.persist(offer);
        em.flush();
        assertThat(historyCount(offer)).isEqualTo(1);
        offer.updatePrice(new BigDecimal("90.00"), Instant.now());
        em.flush();
        assertThat(historyCount(offer)).isEqualTo(2);
        offer.updatePrice(new BigDecimal("90.00"), Instant.now());
        em.flush();
        assertThat(historyCount(offer)).isEqualTo(2);
        em.clear();
        assertThat(products.findById(product.getId()).orElseThrow().getCategory().getId()).isEqualTo(category.getId());
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success", Integer.class)).isPositive();
    }
    @Test void foreignKeysRejectMissingCategory() {
        assertThatThrownBy(() -> jdbc.update("insert into products(category_id,name,slug,brand,model,variant) values(-1,'test','test-invalid','test','test','test')"))
            .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
    private int historyCount(Offer offer) {
        return jdbc.queryForObject("select count(*) from price_history where offer_id = ?", Integer.class, offer.getId());
    }
}
