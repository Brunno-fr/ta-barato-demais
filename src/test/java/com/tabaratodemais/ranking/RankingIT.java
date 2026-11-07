package com.tabaratodemais.ranking;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.offer.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class RankingIT {
    @Autowired RankingService service;
    @Autowired ProductAdminService products;
    @Autowired CategoryRepository categories;
    @Autowired OfferRepository offers;
    @Autowired MockMvc mvc;
    private Long product(){
        var c=categories.saveAndFlush(new Category("Teste ranking","rank-"+UUID.randomUUID(),null));
        var p=new ProductForm();p.categoryId=c.getId();p.name="Produto "+UUID.randomUUID();p.slug="rank-"+UUID.randomUUID();p.brand="Teste";p.model="M1";p.variant="Preto";p.imageUrl="https://example.com/image.jpg";
        Long id=products.save(null,p);
        var f=new OfferForm();f.storeSlug="mercado-livre";f.price=new BigDecimal("123.45");f.affiliateUrl="https://meli.la/test?ref=abc%2F1&x=2";f.checkedAt=LocalDateTime.now(ProductAdminService.ZONE).minusMinutes(5);f.active=true;products.saveOffer(id,null,f);
        p=products.form(id);p.status=ProductStatus.PUBLISHED;products.save(id,p);return id;
    }
    private RankingForm form(){
        var f=new RankingForm();f.title="Seleção "+UUID.randomUUID();f.slug="selecao-"+UUID.randomUUID();f.criteria="Critério editorial <script>alert(1)</script>";
        f.items.get(0).productId=product();f.items.get(0).reason="Primeiro motivo";
        f.items.get(1).productId=product();f.items.get(1).reason="Segundo motivo";return f;
    }
    @Test void draftPublishReorderAndUnpublish() throws Exception {
        var f=form();Long id=service.save(null,f);
        mvc.perform(get("/rankings/"+f.slug)).andExpect(status().isNotFound());
        mvc.perform(get("/rankings")).andExpect(content().string(not(containsString(f.title))));
        f=service.form(id);f.published=true;service.save(id,f);
        assertEquals(f.items.get(0).productId,products.list().stream().filter(p->p.getSlug().equals(service.publicPage(service.form(id).slug).items().getFirst().slug())).findFirst().orElseThrow().getId());
        mvc.perform(get("/rankings/"+f.slug)).andExpect(status().isOk())
            .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
            .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
            .andExpect(content().string(containsString("ref=abc%2F1&amp;x=2")))
            .andExpect(content().string(containsString("sponsored nofollow noopener noreferrer")))
            .andExpect(content().string(containsString("Conferido:")));
        mvc.perform(get("/rankings")).andExpect(content().string(containsString(f.title)));
        f=service.form(id);java.util.Collections.swap(f.items,0,1);service.save(id,f);
        assertEquals("Segundo motivo",service.publicPage(f.slug).items().getFirst().reason());
        f=service.form(id);f.published=false;service.save(id,f);
        mvc.perform(get("/rankings/"+f.slug)).andExpect(status().isNotFound());
    }
    @Test void archivedProductsAreHiddenAndInactiveOffersHaveNoPurchaseButton() throws Exception {
        var f=form();f.published=true;service.save(null,f);
        Long first=f.items.get(0).productId, second=f.items.get(1).productId;
        var p=products.form(first);p.status=ProductStatus.ARCHIVED;products.save(first,p);
        var offer=offers.findForProduct(second).getFirst();var edit=products.offerForm(second,offer.getId());edit.active=false;products.saveOffer(second,offer.getId(),edit);
        var page=service.publicPage(f.slug);assertEquals(1,page.items().size());assertEquals(2,page.items().getFirst().position());
        mvc.perform(get("/rankings/"+f.slug)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString(p.name))))
            .andExpect(content().string(containsString("Não há ofertas ativas")))
            .andExpect(content().string(not(containsString("https://meli.la/test"))));
    }
    @Test void invalidSelectionsAndStaleEditsAreRejected(){
        var f=form();f.items.get(1).productId=f.items.get(0).productId;
        assertThrows(IllegalArgumentException.class,()->service.save(null,f));
        f.items.get(1).productId=null;f.items.get(1).reason="";f.published=true;
        assertThrows(IllegalArgumentException.class,()->service.save(null,f));
        f.published=false;Long id=service.save(null,f);
        var stale=service.form(id);var fresh=service.form(id);fresh.title="Título atualizado";service.save(id,fresh);
        assertThrows(IllegalArgumentException.class,()->service.save(id,stale));
        assertThrows(IllegalArgumentException.class,()->service.save(null,f)); // Slug duplicado.
        var missing=form();missing.items.get(0).productId=Long.MAX_VALUE;
        assertThrows(IllegalArgumentException.class,()->service.save(null,missing));
        var blank=form();blank.items.get(0).reason=" ";
        assertThrows(IllegalArgumentException.class,()->service.save(null,blank));
        var draft=form();var p=products.form(draft.items.get(0).productId);p.status=ProductStatus.DRAFT;products.save(draft.items.get(0).productId,p);draft.published=true;
        assertThrows(IllegalArgumentException.class,()->service.save(null,draft));
    }
    @Test void adminFormBindsAndValidatesAndRoutesAreProtected() throws Exception {
        mvc.perform(get("/admin/rankings")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/rankings").with(user("viewer").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/rankings").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/rankings/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
            .andExpect(content().string(containsString("items[9].productId")));
        String slug="form-"+UUID.randomUUID();Long product=product();
        mvc.perform(post("/admin/rankings").with(user("admin").roles("ADMIN")).with(csrf())
            .param("title","Teste formulário").param("slug",slug).param("criteria","Critérios verificados")
            .param("items[0].productId",product.toString()).param("items[0].reason","Razão editorial"))
            .andExpect(redirectedUrl("/admin/rankings"));
        Long id=service.list(false).stream().filter(r->r.slug().equals(slug)).findFirst().orElseThrow().id();
        assertEquals(product,service.form(id).items.getFirst().productId);
        mvc.perform(get("/admin/rankings/"+id+"/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/rankings").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(post("/admin/rankings").with(user("admin").roles("ADMIN")).with(csrf())
            .param("title","").param("slug","INVALID").param("criteria",""))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("rankingForm","title","slug","criteria"));
        mvc.perform(get("/rankings/inexistente-"+UUID.randomUUID())).andExpect(status().isNotFound());
    }
}
