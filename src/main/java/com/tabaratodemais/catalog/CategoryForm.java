package com.tabaratodemais.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryForm {
    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "Use até 100 caracteres no nome.")
    private String name;
    @NotBlank(message = "Informe a URL amigável.")
    @Size(max = 140, message = "Use até 140 caracteres na URL.")
    @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "Use letras minúsculas sem acentos, números e hífens entre palavras.")
    private String slug;
    private Long parentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? null : name.strip(); }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug == null ? null : slug.strip(); }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
