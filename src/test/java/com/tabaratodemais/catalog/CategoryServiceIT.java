package com.tabaratodemais.catalog;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryServiceIT {
    @Autowired CategoryService service;
    @Autowired CategoryRepository repository;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test void starterCategoriesPreserveExistingNamesAndAreIdempotent() throws Exception {
        // Tabela temporária isola o teste dos cadastros reais do proprietário.
        jdbc.execute("CREATE TEMP TABLE categories (LIKE public.categories INCLUDING ALL) ON COMMIT DROP");
        jdbc.update("INSERT INTO categories(name,slug) VALUES ('Eletronicos','fones-de-ouvido')");
        String sql=new org.springframework.core.io.ClassPathResource("db/migration/V3__starter_categories.sql")
            .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        jdbc.execute(sql); jdbc.execute(sql);
        org.junit.jupiter.api.Assertions.assertEquals(10,jdbc.queryForObject("SELECT count(*) FROM categories",Integer.class));
        org.junit.jupiter.api.Assertions.assertEquals("Eletronicos",jdbc.queryForObject("SELECT name FROM categories WHERE slug='fones-de-ouvido'",String.class));
        org.junit.jupiter.api.Assertions.assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM categories WHERE slug='eletronicos'",Integer.class));
    }

    private CategoryForm form(String name, String slug, Long parent) {
        var f = new CategoryForm(); f.setName(name); f.setSlug(slug); f.setParentId(parent); return f;
    }
    private Category create(Long parent) {
        String slug = "teste-" + UUID.randomUUID();
        service.save(null, form("Categoria de teste", slug, parent));
        return repository.findBySlug(slug).orElseThrow();
    }
    @Test void createEditAndRemoveParentPersistCorrectly() {
        Category parent = create(null);
        Category child = create(parent.getId());
        assertEquals(parent.getId(), service.form(child.getId()).getParentId());
        assertTrue(service.list().stream().anyMatch(c -> c.id().equals(child.getId()) && c.parentName().equals(parent.getName())));
        service.save(child.getId(), form("Nome editado", child.getSlug(), null));
        assertEquals("Nome editado", service.form(child.getId()).getName());
        assertNull(service.form(child.getId()).getParentId());
    }
    @Test void duplicateSlugIsRejected() {
        Category existing = create(null);
        assertThrows(IllegalArgumentException.class, () -> service.save(null, form("Outro nome", existing.getSlug(), null)));
    }
    @Test void categoryCannotBeItsOwnParent() {
        Category c = create(null);
        assertThrows(IllegalArgumentException.class, () -> service.save(c.getId(), form(c.getName(), c.getSlug(), c.getId())));
    }
    @Test void descendantCannotBecomeParent() {
        Category root = create(null);
        Category child = create(root.getId());
        Category grandchild = create(child.getId());
        assertThrows(IllegalArgumentException.class, () -> service.save(root.getId(), form(root.getName(), root.getSlug(), grandchild.getId())));
    }
    @Test void nonexistentParentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.save(null, form("Teste", "teste-"+UUID.randomUUID(), -1L)));
    }
    @Test void nonexistentCategoryReturns404() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.form(-1L)).getStatusCode().value());
    }
}
