package com.tabaratodemais.admin;

import com.tabaratodemais.catalog.CategoryService;
import com.tabaratodemais.catalog.CategoryForm;
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

@WebMvcTest(CategoryAdminController.class)
@Import(SecurityConfig.class)
class CategoryAdminControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CategoryService service;
    @MockitoBean UserDetailsService users;

    @Test void onlyAdminCanReadAndWrite() throws Exception {
        mvc.perform(get("/admin/categorias")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/categorias").with(user("visitor").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/categorias").with(user("visitor").roles("USER")).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/admin/categorias").with(user("admin").roles("ADMIN")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void listEscapesNamesAndFormIncludesCsrf() throws Exception {
        when(service.list()).thenReturn(List.of(new CategoryService.Row(1L,"<script>bad</script>","teste",null)));
        mvc.perform(get("/admin/categorias").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;")));
        mvc.perform(get("/admin/categorias/nova").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")));
    }
    @Test void invalidFieldsDoNotReachPersistence() throws Exception {
        mvc.perform(post("/admin/categorias").with(user("admin").roles("ADMIN")).with(csrf())
            .param("name"," ").param("slug","Inválido /"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("categoryForm","name","slug"));
        verify(service, never()).save(any(), any());
    }
    @Test void malformedParentIsShownAsFormError() throws Exception {
        mvc.perform(post("/admin/categorias").with(user("admin").roles("ADMIN")).with(csrf())
            .param("name","Fones").param("slug","fones").param("parentId","abc"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("categoryForm","parentId"));
        verify(service, never()).save(any(), any());
    }
    @Test void duplicateSlugShowsMessageAndPreservesInput() throws Exception {
        doThrow(new IllegalArgumentException("Esta URL amigável já está em uso. Escolha outra."))
            .when(service).save(isNull(), any());
        mvc.perform(post("/admin/categorias").with(user("admin").roles("ADMIN")).with(csrf())
            .param("name","Fones").param("slug","fones"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("já está em uso")))
            .andExpect(content().string(containsString("value=\"Fones\"")));
    }
    @Test void validCreateAndEditRedirectWithConfirmation() throws Exception {
        mvc.perform(post("/admin/categorias").with(user("admin").roles("ADMIN")).with(csrf())
            .param("name","Fones").param("slug","fones"))
            .andExpect(redirectedUrl("/admin/categorias")).andExpect(flash().attribute("success","Categoria cadastrada."));
        when(service.form(1L)).thenReturn(new CategoryForm());
        mvc.perform(get("/admin/categorias/1/editar").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("action=\"/admin/categorias/1\"")));
        mvc.perform(post("/admin/categorias/1").with(user("admin").roles("ADMIN")).with(csrf())
            .param("name","Fones novos").param("slug","fones"))
            .andExpect(redirectedUrl("/admin/categorias")).andExpect(flash().attribute("success","Categoria atualizada."));
    }
}
