package com.tabaratodemais.catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final JdbcTemplate jdbc;
    public CategoryService(CategoryRepository repository, JdbcTemplate jdbc) {
        this.repository = repository; this.jdbc = jdbc;
    }

    public record Row(Long id, String name, String slug, String parentName) {}

    @Transactional(readOnly = true)
    public List<Row> list() {
        return repository.findAllWithParent().stream()
            .map(c -> new Row(c.getId(), c.getName(), c.getSlug(),
                c.getParent() == null ? null : c.getParent().getName())).toList();
    }

    @Transactional(readOnly = true)
    public CategoryForm form(Long id) {
        Category c = require(id);
        CategoryForm form = new CategoryForm();
        form.setName(c.getName()); form.setSlug(c.getSlug());
        form.setParentId(c.getParent() == null ? null : c.getParent().getId());
        return form;
    }

    @Transactional
    public void save(Long id, CategoryForm form) {
        // Serializa alterações da árvore para impedir ciclos em edições simultâneas.
        jdbc.execute("SELECT pg_advisory_xact_lock(84201031)");
        Category category = id == null ? new Category(null, null, null) : require(id);
        repository.findBySlug(form.getSlug()).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), id)) {
                throw new IllegalArgumentException("Esta URL amigável já está em uso. Escolha outra.");
            }
        });
        Category parent = null;
        if (form.getParentId() != null) {
            parent = repository.findById(form.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("A categoria principal selecionada não existe mais."));
            var visited = new HashSet<Long>();
            for (Category ancestor = parent; ancestor != null; ancestor = ancestor.getParent()) {
                if (Objects.equals(ancestor.getId(), id) || !visited.add(ancestor.getId())) {
                    throw new IllegalArgumentException("A categoria principal não pode ser a própria categoria nem uma de suas subcategorias.");
                }
            }
        }
        category.update(form.getName(), form.getSlug(), parent);
        repository.saveAndFlush(category);
    }

    private Category require(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada."));
    }
}
