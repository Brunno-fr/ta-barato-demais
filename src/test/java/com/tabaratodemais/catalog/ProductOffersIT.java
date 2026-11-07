package com.tabaratodemais.catalog;

import com.tabaratodemais.offer.*;
import com.tabaratodemais.home.CatalogService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductOffersIT {
    @Autowired ProductAdminService service;
    @Autowired CategoryRepository categories;
    @Autowired OfferRepository offers;
    @Autowired CatalogService catalog;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired PriceHistoryService history;

    @Test void featuredOfferCanBeSelectedAndRemovedUsingAdminForm() throws Exception {
        Long id=create(); service.saveOffer(id,null,deal("mercado-livre"));
        Long offerId=offers.findForProduct(id).getFirst().getId();
        var p=service.form(id);p.status=ProductStatus.PUBLISHED;service.save(id,p);
        String url="/admin/produtos/"+id+"/ofertas/"+offerId;
        var f=service.offerForm(id,offerId);
        mvc.perform(post(url).with(user("admin").roles("ADMIN")).with(csrf())
            .param("storeSlug",f.storeSlug).param("price",f.price.toString()).param("affiliateUrl",f.affiliateUrl)
            .param("checkedAt",f.checkedAt.toString()).param("active","true").param("featured","true")
            .param("version",f.version.toString())).andExpect(status().is3xxRedirection());
        assertTrue(service.offerForm(id,offerId).featured);
        mvc.perform(get(url+"/editar").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.matchesPattern(
                "(?s).*<input(?=[^>]*name=\"featured\")(?=[^>]*checked=\"checked\")[^>]*>.*")));
        f=service.offerForm(id,offerId);
        mvc.perform(post(url).with(user("admin").roles("ADMIN")).with(csrf())
            .param("storeSlug",f.storeSlug).param("price",f.price.toString()).param("affiliateUrl",f.affiliateUrl)
            .param("checkedAt",f.checkedAt.toString()).param("active","true").param("_featured","on")
            .param("version",f.version.toString())).andExpect(status().is3xxRedirection());
        assertFalse(service.offerForm(id,offerId).featured);
        assertEquals(1,history.get(id,offerId,1).total());
        mvc.perform(post(url).with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post(url).with(user("viewer").roles("USER")).with(csrf())).andExpect(status().isForbidden());
    }

    @Test void featuredSelectionIsLimitedAndHidesInactiveDraftAndArchivedOffers() throws Exception {
        // Isola a seleção dentro da transação, revertida integralmente ao final do teste.
        jdbc.update("update offers set featured=false where featured=true");
        var ids=new java.util.ArrayList<Long>();
        for(int n=0;n<7;n++) {
            Long id=create(); ids.add(id);
            var f=deal("mercado-livre");f.featured=true;f.checkedAt=f.checkedAt.minusMinutes(n);
            service.saveOffer(id,null,f);
            var p=service.form(id);p.status=ProductStatus.PUBLISHED;service.save(id,p);
        }
        assertEquals(6,catalog.featured().size());
        assertEquals(service.product(ids.getFirst()).getSlug(),catalog.featured().getFirst().slug());
        assertFalse(catalog.featured().stream().anyMatch(c -> c.slug().equals(service.product(ids.getLast()).getSlug())));
        Long id=ids.getFirst();Long offerId=offers.findForProduct(id).getFirst().getId();
        var f=deal("shopee");service.saveOffer(id,null,f); // Não marcada: não entra no destaque.
        assertEquals(1,catalog.featured().getFirst().deals().size());
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("id=\"ofertas-em-destaque\"")))
            .andExpect(content().string(containsString("ref=abc%2F1&amp;source=test")));
        f=service.offerForm(id,offerId);f.active=false;service.saveOffer(id,offerId,f);
        assertFalse(catalog.featured().stream().anyMatch(c -> c.slug().equals(service.product(id).getSlug())));
        f=service.offerForm(id,offerId);f.active=true;service.saveOffer(id,offerId,f);
        for(var status : new ProductStatus[]{ProductStatus.DRAFT,ProductStatus.ARCHIVED}) {
            var p=service.form(id);p.status=status;service.save(id,p);
            assertFalse(catalog.featured().stream().anyMatch(c -> c.slug().equals(p.slug)));
        }
        mvc.perform(get("/").param("q","teste")).andExpect(content().string(not(containsString("id=\"ofertas-em-destaque\""))));
    }

    @Test void historyShowsRecordedPricesAndRequiresAdmin() throws Exception {
        Long id=create(); service.saveOffer(id,null,deal("mercado-livre"));
        Long offerId=offers.findForProduct(id).getFirst().getId();
        var edited=service.offerForm(id,offerId); edited.price=new BigDecimal("95.00");
        service.saveOffer(id,offerId,edited);
        edited=service.offerForm(id,offerId); edited.active=false;
        service.saveOffer(id,offerId,edited);
        var result=history.get(id,offerId,1);
        assertEquals(2,result.total());
        assertEquals(new BigDecimal("95.00"),result.entries().getFirst().price());
        assertEquals(new BigDecimal("100.00"),result.entries().getLast().price());
        String url="/admin/produtos/"+id+"/ofertas/"+offerId+"/historico";
        mvc.perform(get(url)).andExpect(status().is3xxRedirection());
        mvc.perform(get(url).with(user("viewer").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get(url).with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("R$ 95,00")))
            .andExpect(content().string(containsString("R$ 100,00")))
            .andExpect(content().string(containsString("Mercado Livre")));
        mvc.perform(get("/admin/produtos/"+id+"/ofertas").with(user("admin").roles("ADMIN")))
            .andExpect(content().string(containsString(url)));
        mvc.perform(get(url).param("pagina","0").with(user("admin").roles("ADMIN")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/admin/produtos/"+create()+"/ofertas/"+offerId+"/historico").with(user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound());
        mvc.perform(get("/admin/produtos/"+id+"/ofertas/0/historico").with(user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @Test void historyPaginatesSeparatelyByOfferAndFormatsBrasiliaTime() throws Exception {
        Long id=create(); service.saveOffer(id,null,deal("mercado-livre"));
        Long offerId=offers.findForProduct(id).getFirst().getId();
        service.saveOffer(id,null,deal("shopee"));
        jdbc.update("delete from price_history where offer_id=?",offerId);
        // Dados sintéticos revertidos pela transação do teste.
        jdbc.update("""
            insert into price_history(offer_id,price,recorded_at)
            select ?, n, timestamptz '2026-01-01 03:00:00+00' + n * interval '1 second'
            from generate_series(1,26) n
            """,offerId);
        var first=history.get(id,offerId,1);
        var second=history.get(id,offerId,2);
        assertEquals(26,first.total()); assertEquals(25,first.entries().size());
        assertEquals(1,second.entries().size()); assertEquals(2,first.pages());
        assertEquals("01/01/2026 00:00:26",first.entries().getFirst().recordedAt());
        assertEquals(new BigDecimal("1.00"),second.entries().getFirst().price());
        assertEquals(2,history.get(id,offerId,999).page());
        String url="/admin/produtos/"+id+"/ofertas/"+offerId+"/historico";
        mvc.perform(get(url).with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString(url+"?pagina=2")));
        mvc.perform(get(url).param("pagina","2").with(user("admin").roles("ADMIN")))
            .andExpect(content().string(containsString(url+"?pagina=1")));
        jdbc.update("delete from price_history where offer_id=?",offerId);
        mvc.perform(get(url).with(user("admin").roles("ADMIN")))
            .andExpect(content().string(containsString("Ainda não há registros")));
    }

    private Long create() {
        Category category=categories.saveAndFlush(new Category("Teste", "teste-"+UUID.randomUUID(),null));
        ProductForm f=new ProductForm(); f.categoryId=category.getId(); f.name="Produto sintético "+UUID.randomUUID();
        f.slug="teste-"+UUID.randomUUID(); f.brand="Teste"; f.model="M1"; f.variant="256GB preto";
        f.imageUrl="https://example.com/test-image.jpg";
        return service.save(null,f);
    }
    private OfferForm deal(String store) {
        OfferForm f=new OfferForm(); f.storeSlug=store; f.price=new BigDecimal("100.00");
        f.affiliateUrl=store.equals("mercado-livre") ? "https://meli.la/synthetic-test?ref=abc%2F1&source=test" : "https://s.shopee.com.br/synthetic-test";
        f.checkedAt=LocalDateTime.now(ProductAdminService.ZONE).minusMinutes(5).withSecond(0).withNano(0); f.active=true; return f;
    }
    @Test void publishGroupsBothStoresKeepsLinksAndArchiveHidesProduct() throws Exception {
        Long id=create(); String name=service.product(id).getName();
        service.saveOffer(id,null,deal("mercado-livre")); service.saveOffer(id,null,deal("shopee"));
        assertTrue(catalog.search(new com.tabaratodemais.home.CatalogFilter(name,null,"","preco-asc",1)).cards().stream().noneMatch(c -> c.name().equals(name)));
        ProductForm f=service.form(id); f.status=ProductStatus.PUBLISHED; service.save(id,f);
        var card=catalog.search(new com.tabaratodemais.home.CatalogFilter(name,null,"","preco-asc",1)).cards().stream().filter(c -> c.name().equals(name)).findFirst().orElseThrow();
        assertEquals(2,card.deals().size()); assertEquals("Frete não informado",card.deals().getFirst().shipping());
        assertEquals(deal("mercado-livre").affiliateUrl,card.deals().getFirst().url());
        mvc.perform(get("/").param("q",service.product(id).getName())).andExpect(status().isOk()).andExpect(content().string(containsString(name)))
            .andExpect(content().string(containsString("ref=abc%2F1&amp;source=test")))
            .andExpect(content().string(containsString("sponsored nofollow noopener noreferrer")))
            .andExpect(content().string(containsString("Conferido:")));
        mvc.perform(get("/admin/produtos").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/produtos/"+id+"/ofertas").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/produtos/"+id+"/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        f.status=ProductStatus.ARCHIVED; service.save(id,f);
        // O nome continua no campo de busca, mas o card arquivado não deve aparecer.
        mvc.perform(get("/").param("q",service.product(id).getName()))
            .andExpect(content().string(not(containsString("class=\"product-card\""))));
    }
    @Test void submittedFormsCreateProductAndOfferWithExactAffiliateLink() throws Exception {
        Category c=categories.saveAndFlush(new Category("Formulário", "teste-"+UUID.randomUUID(),null));
        String slug="teste-"+UUID.randomUUID();
        var response=mvc.perform(post("/admin/produtos").with(user("admin").roles("ADMIN")).with(csrf())
            .param("categoryId",c.getId().toString()).param("name","Produto do formulário")
            .param("slug",slug).param("brand","Teste").param("model","M1").param("variant","256GB preto")
            .param("imageUrl","https://example.com/test.jpg").param("status","DRAFT"))
            .andExpect(status().is3xxRedirection()).andReturn();
        Long id=Long.valueOf(response.getResponse().getRedirectedUrl().split("/")[3]);
        String link="https://meli.la/test?tracking=abc%2F123&ref=example";
        String checked=LocalDateTime.now(ProductAdminService.ZONE).minusMinutes(5).withSecond(0).withNano(0).toString();
        mvc.perform(post("/admin/produtos/"+id+"/ofertas").with(user("admin").roles("ADMIN")).with(csrf())
            .param("storeSlug","mercado-livre").param("price","123.45").param("affiliateUrl",link)
            .param("shippingCost","").param("checkedAt",checked).param("active","true"))
            .andExpect(redirectedUrl("/admin/produtos/"+id+"/ofertas"));
        var saved=offers.findForProduct(id).getFirst();
        assertEquals(link,saved.getAffiliateUrl());assertEquals(new BigDecimal("123.45"),saved.getPrice());
        assertNull(saved.getShippingCost());assertTrue(saved.getActive());
    }
    @Test void editingPriceRecordsHistoryAndInactiveOffersDisappear() throws Exception {
        Long id=create(); service.saveOffer(id,null,deal("mercado-livre"));
        var o=offers.findForProduct(id).getFirst();
        var f=service.offerForm(id,o.getId()); f.price=new BigDecimal("95.00"); service.saveOffer(id,o.getId(),f);
        assertEquals(2,jdbc.queryForObject("select count(*) from price_history where offer_id=?",Integer.class,o.getId()));
        var p=service.form(id);p.status=ProductStatus.PUBLISHED;service.save(id,p);
        f=service.offerForm(id,o.getId()); f.active=false; service.saveOffer(id,o.getId(),f);
        assertTrue(catalog.search(new com.tabaratodemais.home.CatalogFilter(p.name,null,"","preco-asc",1)).cards().stream().noneMatch(c -> c.name().equals(p.name)));
        assertEquals(2,jdbc.queryForObject("select count(*) from price_history where offer_id=?",Integer.class,o.getId()));
        mvc.perform(get("/admin/produtos/"+id+"/ofertas/"+o.getId()+"/editar").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("value=\"95.00\"")));
    }
    @Test void cannotPublishWithoutActiveOffer() {
        Long id=create(); var f=service.form(id); f.status=ProductStatus.PUBLISHED;
        assertThrows(IllegalArgumentException.class,() -> service.save(id,f));
    }
    @Test void futureVerificationIsRejected() {
        Long id=create();var f=deal("mercado-livre");f.checkedAt=LocalDateTime.now(ProductAdminService.ZONE).plusDays(1);
        assertThrows(IllegalArgumentException.class,() -> service.saveOffer(id,null,f));
    }
    @Test void shippingChoicesPersistAndDisplayCorrectly() throws Exception {
        Long id=create(); var f=deal("mercado-livre"); f.shippingType="FREE";
        f.shippingCost=new BigDecimal("25.00"); // Campo oculto antigo não pode sobrescrever a escolha.
        service.saveOffer(id,null,f);
        Long offerId=offers.findForProduct(id).getFirst().getId();
        assertEquals("FREE",service.offerForm(id,offerId).shippingType);
        assertEquals(0,offers.findById(offerId).orElseThrow().getShippingCost().signum());
        var p=service.form(id);p.status=ProductStatus.PUBLISHED;service.save(id,p);
        assertEquals("Frete grátis",catalog.search(new com.tabaratodemais.home.CatalogFilter(p.name,null,"","preco-asc",1)).cards().stream().filter(c -> c.name().equals(p.name)).findFirst().orElseThrow().deals().getFirst().shipping());
        mvc.perform(get("/").param("q",service.product(id).getName())).andExpect(content().string(containsString("Frete grátis")));
        f=service.offerForm(id,offerId);f.shippingType="PAID";f.shippingCost=new BigDecimal("12.50");service.saveOffer(id,offerId,f);
        assertEquals("PAID",service.offerForm(id,offerId).shippingType);
        assertEquals(new BigDecimal("12.50"),offers.findById(offerId).orElseThrow().getShippingCost());
        f=service.offerForm(id,offerId);f.shippingType="UNKNOWN";service.saveOffer(id,offerId,f);
        assertNull(offers.findById(offerId).orElseThrow().getShippingCost());
    }
    @Test void paidShippingNeedsAPositiveAmount() {
        Long id=create();var f=deal("mercado-livre");f.shippingType="PAID";
        assertThrows(IllegalArgumentException.class,() -> service.saveOffer(id,null,f));
    }
    @Test void publicProductPageShowsOnlyActiveOffersAndEscapesDescription() throws Exception {
        Long id=create(); String slug=service.form(id).slug;
        mvc.perform(get("/produtos/"+slug)).andExpect(status().isNotFound());
        var first=deal("mercado-livre"); first.shippingType="FREE";
        service.saveOffer(id,null,first);
        var second=deal("shopee"); second.price=new BigDecimal("90.00");
        service.saveOffer(id,null,second);
        var inactive=deal("shopee");inactive.active=false;inactive.affiliateUrl="https://shope.ee/inactive-test";
        service.saveOffer(id,null,inactive);
        var form=service.form(id);form.description="Descrição <script>alert(1)</script>";form.status=ProductStatus.PUBLISHED;service.save(id,form);
        mvc.perform(get("/").param("q",service.product(id).getName())).andExpect(content().string(containsString("href=\"/produtos/"+slug+"\"")));
        mvc.perform(get("/produtos/"+slug)).andExpect(status().isOk())
            .andExpect(content().string(containsString(form.name)))
            .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
            .andExpect(content().string(containsString("Mercado Livre")))
            .andExpect(content().string(containsString("Shopee")))
            .andExpect(content().string(containsString("Frete grátis")))
            .andExpect(content().string(containsString("Conferido:")))
            .andExpect(content().string(containsString("ref=abc%2F1&amp;source=test")))
            .andExpect(content().string(not(containsString("https://shope.ee/inactive-test"))))
            .andExpect(content().string(containsString("links de afiliados")));
        assertEquals("Shopee",catalog.detail(slug).deals().getFirst().store());
        form.status=ProductStatus.ARCHIVED;service.save(id,form);
        mvc.perform(get("/produtos/"+slug)).andExpect(status().isNotFound());
        mvc.perform(get("/produtos/nao-existe-"+UUID.randomUUID())).andExpect(status().isNotFound());
    }
    @Test void publishedProductWithoutActiveOffersKeepsAnHonestEmptyPage() throws Exception {
        Long id=create();service.saveOffer(id,null,deal("mercado-livre"));
        var form=service.form(id);form.status=ProductStatus.PUBLISHED;service.save(id,form);
        Long offerId=offers.findForProduct(id).getFirst().getId();
        var f=service.offerForm(id,offerId);f.active=false;service.saveOffer(id,offerId,f);
        mvc.perform(get("/produtos/"+form.slug)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Não há ofertas ativas")))
            .andExpect(content().string(not(containsString(f.affiliateUrl))));
    }
    @Test void offerCannotBeEditedUnderAnotherProduct() {
        Long first=create();Long second=create();service.saveOffer(first,null,deal("mercado-livre"));
        Long offerId=offers.findForProduct(first).getFirst().getId();
        assertEquals(404,assertThrows(ResponseStatusException.class,() -> service.offerForm(second,offerId)).getStatusCode().value());
    }
    @Test void staleOfferFormCannotOverwriteChanges() {
        Long id=create();service.saveOffer(id,null,deal("mercado-livre"));Long offerId=offers.findForProduct(id).getFirst().getId();
        var stale=service.offerForm(id,offerId);var fresh=service.offerForm(id,offerId);fresh.price=new BigDecimal("90.00");service.saveOffer(id,offerId,fresh);
        assertThrows(IllegalArgumentException.class,() -> service.saveOffer(id,offerId,stale));
    }
}
