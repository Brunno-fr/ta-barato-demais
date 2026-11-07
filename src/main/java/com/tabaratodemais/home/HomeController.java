package com.tabaratodemais.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final CatalogService catalog;
    private final com.tabaratodemais.catalog.CategoryService categories;
    public HomeController(CatalogService catalog, com.tabaratodemais.catalog.CategoryService categories) {
        this.catalog=catalog; this.categories=categories;
    }
    @GetMapping("/")
    public String index(@RequestParam(defaultValue="") String q,
                        @RequestParam(required=false) Long categoria,
                        @RequestParam(defaultValue="") String loja,
                        @RequestParam(defaultValue="preco-asc") String ordem,
                        @RequestParam(defaultValue="1") int pagina,
                        org.springframework.ui.Model model) {
        var filter=new CatalogFilter(q,categoria,loja,ordem,pagina);
        var result=catalog.search(filter);
        model.addAttribute("filters",filter);
        model.addAttribute("result",result);
        model.addAttribute("cards",result.cards());
        model.addAttribute("featuredCards",filter.query().isEmpty() && categoria == null && filter.store().isEmpty()
            && pagina == 1 && filter.sort().equals("preco-asc") ? catalog.featured() : java.util.List.of());
        model.addAttribute("previousUrl",filter.url(categoria,Math.max(1,result.page()-1)));
        model.addAttribute("nextUrl",filter.url(categoria,result.page()+1));
        model.addAttribute("allCategoriesUrl",filter.url(null,1));
        model.addAttribute("categories",categories.list().stream()
            .map(c -> new CategoryLink(c.id(), c.name(), CategoryIcons.forCategory(c),filter.url(c.id(),1))).toList());
        return "index";
    }
    public record CategoryLink(Long id, String name, String icon, String url) {}
}
