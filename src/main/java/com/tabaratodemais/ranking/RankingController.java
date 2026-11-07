package com.tabaratodemais.ranking;

import com.tabaratodemais.catalog.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RankingController {
    private final RankingService service;
    private final ProductRepository products;
    public RankingController(RankingService service,ProductRepository products){this.service=service;this.products=products;}
    @InitBinder("rankingForm") public void bind(WebDataBinder binder){binder.setAutoGrowCollectionLimit(10);}
    @GetMapping("/admin/rankings") public String list(Model m){m.addAttribute("rankings",service.list(false));return "admin/rankings/list";}
    @GetMapping("/admin/rankings/novo") public String create(Model m){m.addAttribute("rankingForm",new RankingForm());return form(null,m);}
    @GetMapping("/admin/rankings/{id}/editar") public String edit(@PathVariable Long id,Model m){m.addAttribute("rankingForm",service.form(id));return form(id,m);}
    @PostMapping("/admin/rankings") public String create(@Valid @ModelAttribute RankingForm rankingForm,BindingResult errors,Model m,RedirectAttributes r){return save(null,rankingForm,errors,m,r);}
    @PostMapping("/admin/rankings/{id}") public String edit(@PathVariable Long id,@Valid @ModelAttribute RankingForm rankingForm,BindingResult errors,Model m,RedirectAttributes r){service.form(id);return save(id,rankingForm,errors,m,r);}
    private String save(Long id,RankingForm f,BindingResult errors,Model m,RedirectAttributes r){
        if(!errors.hasErrors()) try{service.save(id,f);r.addFlashAttribute("success","Ranking salvo.");return "redirect:/admin/rankings";}catch(IllegalArgumentException e){errors.reject("ranking.invalid",e.getMessage());}
        return form(id,m);
    }
    private String form(Long id,Model m){m.addAttribute("formAction",id==null ? "/admin/rankings" : "/admin/rankings/"+id);m.addAttribute("products",products.findAllWithCategory());return "admin/rankings/form";}
    @GetMapping("/rankings") public String publicList(Model m){m.addAttribute("rankings",service.list(true));return "rankings/list";}
    @GetMapping("/rankings/{slug}") public String page(@PathVariable String slug,Model m){m.addAttribute("page",service.publicPage(slug));return "rankings/detail";}
}
