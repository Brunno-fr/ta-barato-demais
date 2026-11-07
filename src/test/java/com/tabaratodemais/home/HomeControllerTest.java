package com.tabaratodemais.home;

import com.tabaratodemais.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {
    @Autowired MockMvc mvc;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    org.springframework.security.core.userdetails.UserDetailsService users;
    @org.springframework.test.context.bean.override.mockito.MockitoBean CatalogService catalog;
    @org.springframework.test.context.bean.override.mockito.MockitoBean com.tabaratodemais.catalog.CategoryService categories;
    @org.junit.jupiter.api.BeforeEach void emptyCatalog() {
        org.mockito.Mockito.when(catalog.search(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new CatalogService.SearchPage(java.util.List.of(),0,1,1));
    }

    @Test void queryFiltersAndPaginationAreKeptInLinks() throws Exception {
        org.mockito.Mockito.when(catalog.search(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new CatalogService.SearchPage(java.util.List.of(),25,2,3));
        mvc.perform(get("/").param("q","C++ & café").param("loja","shopee").param("ordem","preco-desc").param("pagina","2"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("q=C%2B%2B%20%26%20caf%C3%A9")))
            .andExpect(content().string(containsString("loja=shopee&amp;ordem=preco-desc&amp;pagina=3#destaques")))
            .andExpect(content().string(containsString("method=\"get\"")))
            .andExpect(content().string(containsString("name=\"q\"")));
    }

    @Test void malformedFiltersAreRejected() throws Exception {
        for(String[] pair : new String[][]{{"pagina","0"},{"categoria","-1"},{"ordem","sql"},{"loja","unknown"},{"q","a".repeat(101)},{"pagina","abc"}})
            mvc.perform(get("/").param(pair[0],pair[1])).andExpect(status().isBadRequest());
    }

    @Test void homeIsPublicAndDisclosesEmptyCatalogAndAffiliates() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Ainda não há produtos")))
            .andExpect(content().string(containsString("links de afiliados")))
            .andExpect(header().exists("Content-Security-Policy"));
    }
    @Test void administrativeAreaIsClosed() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login"));
    }
    @Test void categoriesRenderSpecificIconsIncludingLegacySlugAndParentFallback() throws Exception {
        org.mockito.Mockito.when(categories.list()).thenReturn(java.util.List.of(
            new com.tabaratodemais.catalog.CategoryService.Row(1L,"Eletronicos","fones-de-ouvido",null),
            new com.tabaratodemais.catalog.CategoryService.Row(2L,"Automotivo","automotivo",null),
            new com.tabaratodemais.catalog.CategoryService.Row(3L,"Notebooks","notebooks","Informática"),
            new com.tabaratodemais.catalog.CategoryService.Row(4L,"Personalizada","personalizada",null)));
        String html=mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        for (String[] expected : new String[][]{{"1","phone"},{"2","car"},{"3","laptop"},{"4","grid"}}) {
            org.junit.jupiter.api.Assertions.assertTrue(java.util.regex.Pattern.compile(
                "data-category=\""+expected[0]+"\"[^>]*>.*?<use href=\"#i-"+expected[1]+"\"", java.util.regex.Pattern.DOTALL)
                .matcher(html).find());
        }
    }
    @Test void brandImageIsPublic() throws Exception {
        mvc.perform(get("/images/brand.png")).andExpect(status().isOk());
    }
    @Test void mutationsWithoutCsrfAreRejected() throws Exception {
        mvc.perform(post("/admin/products")).andExpect(status().isForbidden());
    }
}
