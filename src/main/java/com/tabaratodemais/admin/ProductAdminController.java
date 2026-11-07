package com.tabaratodemais.admin;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.offer.OfferForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/produtos")
public class ProductAdminController {
    private final ProductAdminService service;
    private final CategoryService categories;
    public ProductAdminController(ProductAdminService service, CategoryService categories){this.service=service;this.categories=categories;}
    @GetMapping public String list(Model m){m.addAttribute("products",service.list());return "admin/products/list";}
    @GetMapping("/novo") public String create(Model m){m.addAttribute("productForm",new ProductForm());return productPage(null,m);}
    @GetMapping("/{id}/editar") public String edit(@PathVariable Long id, Model m){m.addAttribute("productForm",service.form(id));return productPage(id,m);}
    @PostMapping public String create(@Valid @ModelAttribute ProductForm f, BindingResult errors, Model m, RedirectAttributes r){return save(null,f,errors,m,r);}
    @PostMapping("/{id}") public String edit(@PathVariable Long id,@Valid @ModelAttribute ProductForm f,BindingResult errors,Model m,RedirectAttributes r){service.product(id);return save(id,f,errors,m,r);}
    private String save(Long id,ProductForm f,BindingResult errors,Model m,RedirectAttributes r){
        if(!errors.hasErrors()){
            try { Long saved=service.save(id,f);r.addFlashAttribute("success","Produto salvo. Cadastre e confira as ofertas antes de publicar.");return "redirect:/admin/produtos/"+saved+"/ofertas"; }
            catch(IllegalArgumentException e){errors.reject("product.invalid",e.getMessage());}
        }
        return productPage(id,m);
    }
    private String productPage(Long id,Model m){
        m.addAttribute("productId",id);m.addAttribute("categories",categories.list());
        m.addAttribute("formAction",id==null ? "/admin/produtos" : "/admin/produtos/"+id);return "admin/products/form";
    }
    @GetMapping("/{id}/ofertas") public String offers(@PathVariable Long id,Model m){
        m.addAttribute("product",service.product(id));m.addAttribute("offers",service.offers(id));return "admin/products/offers";
    }
    @GetMapping("/{id}/ofertas/nova") public String offerCreate(@PathVariable Long id,Model m){m.addAttribute("offerForm",new OfferForm());return offerPage(id,null,m);}
    @GetMapping("/{id}/ofertas/{offerId}/editar") public String offerEdit(@PathVariable Long id,@PathVariable Long offerId,Model m){m.addAttribute("offerForm",service.offerForm(id,offerId));return offerPage(id,offerId,m);}
    @PostMapping("/{id}/ofertas") public String offerCreate(@PathVariable Long id,@Valid @ModelAttribute OfferForm f,BindingResult errors,Model m,RedirectAttributes r){return saveOffer(id,null,f,errors,m,r);}
    @PostMapping("/{id}/ofertas/{offerId}") public String offerEdit(@PathVariable Long id,@PathVariable Long offerId,@Valid @ModelAttribute OfferForm f,BindingResult errors,Model m,RedirectAttributes r){service.offerForm(id,offerId);return saveOffer(id,offerId,f,errors,m,r);}
    private String saveOffer(Long id,Long offerId,OfferForm f,BindingResult errors,Model m,RedirectAttributes r){
        if(!errors.hasErrors()){
            try { service.saveOffer(id,offerId,f);r.addFlashAttribute("success","Oferta salva. Para aparecer no site, a oferta deve estar ativa e o produto publicado.");return "redirect:/admin/produtos/"+id+"/ofertas"; }
            catch(IllegalArgumentException e){errors.reject("offer.invalid",e.getMessage());}
        }
        return offerPage(id,offerId,m);
    }
    private String offerPage(Long id,Long offerId,Model m){
        m.addAttribute("product",service.product(id));m.addAttribute("offerId",offerId);
        m.addAttribute("formAction","/admin/produtos/"+id+"/ofertas"+(offerId==null ? "" : "/"+offerId));return "admin/products/offer-form";
    }
}
