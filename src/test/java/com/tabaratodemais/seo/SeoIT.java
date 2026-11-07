package com.tabaratodemais.seo;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"site.base-url=https://tabaratodemais.example","site.indexing-enabled=true"})
@AutoConfigureMockMvc @Transactional
class SeoIT {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    private Long category(){return jdbc.queryForObject("insert into categories(name,slug) values ('SEO teste',?) returning id",Long.class,"seo-"+UUID.randomUUID());}
    private void product(Long category,String slug,String status){jdbc.update("insert into products(category_id,name,slug,brand,model,variant,status,image_url) values (?,'Produto SEO',?,'Teste','Modelo','256GB',?,'https://example.com/photo.jpg')",category,slug,status);}

    @Test void metadataUsesConfiguredOriginAndFiltersStayNoindex() throws Exception {
        mvc.perform(get("/").header("Host","evil.example")).andExpect(status().isOk())
            .andExpect(content().string(containsString("rel=\"canonical\" href=\"https://tabaratodemais.example/\"")))
            .andExpect(content().string(containsString("name=\"robots\" content=\"index, follow\"")))
            .andExpect(content().string(containsString("property=\"og:image\" content=\"https://tabaratodemais.example/images/brand.png\"")))
            .andExpect(content().string(not(containsString("evil.example"))));
        mvc.perform(get("/").param("q","teste")).andExpect(header().string("X-Robots-Tag","noindex, nofollow"))
            .andExpect(content().string(containsString("name=\"robots\" content=\"noindex, nofollow\"")));
        mvc.perform(get("/admin/login")).andExpect(header().string("X-Robots-Tag","noindex, nofollow"));
        mvc.perform(get("/robots.txt")).andExpect(status().isOk()).andExpect(content().string("User-agent: *\nDisallow: /admin\nSitemap: https://tabaratodemais.example/sitemap.xml\n"));
    }
    @Test void productAndRankingHaveSpecificMetadata() throws Exception {
        String slug="seo-"+UUID.randomUUID();product(category(),slug,"PUBLISHED");
        mvc.perform(get("/produtos/"+slug)).andExpect(status().isOk())
            .andExpect(content().string(containsString("https://tabaratodemais.example/produtos/"+slug)))
            .andExpect(content().string(containsString("property=\"og:image\" content=\"https://example.com/photo.jpg\"")))
            .andExpect(content().string(containsString("Produto SEO — Compare ofertas")));
        jdbc.update("insert into rankings(title,slug,criteria,published) values ('Seleção SEO',?,'Critério',true)",slug);
        mvc.perform(get("/rankings/"+slug)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Seleção SEO — Tabarato Demais")))
            .andExpect(content().string(containsString("https://tabaratodemais.example/rankings/"+slug)));
        mvc.perform(get("/rankings")).andExpect(content().string(containsString("property=\"og:title\"")));
    }
    @Test void sitemapPaginatesAndIncludesOnlyPublishedContent() throws Exception {
        Long category=category();String prefix="seo-"+UUID.randomUUID();
        product(category,prefix+"-draft","DRAFT");product(category,prefix+"-archived","ARCHIVED");
        jdbc.update("insert into products(category_id,name,slug,brand,model,variant,status) select ?,'SEO',? || n,'Teste','M','V','PUBLISHED' from generate_series(1,1001) n",category,prefix);
        jdbc.update("insert into rankings(title,slug,criteria,published) values ('Publicado',?,'Critérios',true),('Rascunho',?,'Critérios',false)",prefix+"-public",prefix+"-private");
        String index=mvc.perform(get("/sitemap.xml")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/xml")).andReturn().getResponse().getContentAsString();
        var factory=javax.xml.parsers.DocumentBuilderFactory.newInstance();factory.setNamespaceAware(true);
        var builder=factory.newDocumentBuilder();
        var doc=builder.parse(new java.io.ByteArrayInputStream(index.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        int pages=doc.getElementsByTagNameNS("http://www.sitemaps.org/schemas/sitemap/0.9","sitemap").getLength();assertTrue(pages>=2);
        StringBuilder all=new StringBuilder();
        for(int page=1;page<=pages;page++){
            String xml=mvc.perform(get("/sitemaps/"+page+".xml")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            var parsed=builder.parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            assertTrue(parsed.getElementsByTagNameNS("http://www.sitemaps.org/schemas/sitemap/0.9","url").getLength()<=1000);all.append(xml);
        }
        assertTrue(all.toString().contains(prefix+"1001"));assertTrue(all.toString().contains(prefix+"-public"));
        for(String hidden:new String[]{prefix+"-draft",prefix+"-archived",prefix+"-private","/admin","?q="}) assertFalse(all.toString().contains(hidden));
        mvc.perform(get("/sitemaps/0.xml")).andExpect(status().isNotFound());
        mvc.perform(get("/sitemaps/"+(pages+1)+".xml")).andExpect(status().isNotFound());
    }
}
