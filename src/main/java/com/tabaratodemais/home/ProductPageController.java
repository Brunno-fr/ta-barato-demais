package com.tabaratodemais.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductPageController {
    private final CatalogService catalog;
    public ProductPageController(CatalogService catalog) { this.catalog=catalog; }

    @GetMapping("/produtos/{slug}")
    public String product(@PathVariable String slug, Model model) {
        model.addAttribute("product",catalog.detail(slug));
        return "product";
    }
}
