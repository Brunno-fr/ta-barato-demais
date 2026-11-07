package com.tabaratodemais.admin;

import com.tabaratodemais.catalog.CategoryForm;
import com.tabaratodemais.catalog.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class CategoryAdminController {
    private final CategoryService service;
    public CategoryAdminController(CategoryService service) { this.service = service; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", service.list());
        return "admin/categories/list";
    }

    @GetMapping("/nova")
    public String create(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return renderForm(null, model);
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("categoryForm", service.form(id));
        return renderForm(id, model);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CategoryForm categoryForm, BindingResult errors,
                         Model model, RedirectAttributes redirect) {
        return save(null, categoryForm, errors, model, redirect);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute CategoryForm categoryForm,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        service.form(id); // Retorna 404 mesmo quando o formulário enviado é inválido.
        return save(id, categoryForm, errors, model, redirect);
    }

    private String save(Long id, CategoryForm form, BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try { service.save(id, form); }
            catch (IllegalArgumentException ex) { errors.reject("category.invalid", ex.getMessage()); }
        }
        if (errors.hasErrors()) return renderForm(id, model);
        redirect.addFlashAttribute("success", id == null ? "Categoria cadastrada." : "Categoria atualizada.");
        return "redirect:/admin/categorias";
    }

    private String renderForm(Long id, Model model) {
        model.addAttribute("categoryId", id);
        model.addAttribute("parents", service.list().stream().filter(c -> !c.id().equals(id)).toList());
        model.addAttribute("formAction", id == null ? "/admin/categorias" : "/admin/categorias/" + id);
        return "admin/categories/form";
    }
}
