package com.tabaratodemais.admin;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.offer.*;
import com.tabaratodemais.config.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductAdminController.class)
@Import(SecurityConfig.class)
class ProductAdminControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ProductAdminService service;
    @MockitoBean CategoryService categories;
    @MockitoBean UserDetailsService users;

    private Product product() {
        Product p=mock(Product.class); when(p.getId()).thenReturn(1L); when(p.getName()).thenReturn("Produto teste");
        when(p.getBrand()).thenReturn("Marca"); when(p.getModel()).thenReturn("Modelo"); when(p.getVariant()).thenReturn("Versão");
        when(p.getStatus()).thenReturn(ProductStatus.DRAFT); return p;
    }
    @Test void adminPagesRenderFormsAndCsrf() throws Exception {
        Product p = product(); when(service.product(1L)).thenReturn(p);
        when(service.list()).thenReturn(List.of());
        for(String path:List.of("/admin/produtos","/admin/produtos/novo","/admin/produtos/1/ofertas","/admin/produtos/1/ofertas/nova"))
            mvc.perform(get(path).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/produtos/1/ofertas/nova").with(user("admin").roles("ADMIN")))
            .andExpect(content().string(containsString("name=\"_csrf\"")))
            .andExpect(content().string(containsString("name=\"affiliateUrl\"")));
    }
    @Test void protectedRoutesRequireAdminAndCsrf() throws Exception {
        mvc.perform(get("/admin/produtos")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/produtos/1/ofertas").with(csrf()).with(user("visitor").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/produtos/1/ofertas").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void invalidProductAndOfferNeverPersist() throws Exception {
        Product p = product(); when(service.product(1L)).thenReturn(p);
        mvc.perform(post("/admin/produtos").with(user("admin").roles("ADMIN")).with(csrf())
            .param("name"," ").param("slug","invalid slug"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("productForm","name","slug","categoryId"));
        mvc.perform(post("/admin/produtos/1/ofertas").with(user("admin").roles("ADMIN")).with(csrf())
            .param("price","1.999").param("shippingCost","-1").param("checkedAt","invalid"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("offerForm","price","shippingCost","checkedAt"));
        verify(service,never()).save(any(),any()); verify(service,never()).saveOffer(any(),any(),any());
    }
}
