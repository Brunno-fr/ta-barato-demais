package com.tabaratodemais.seo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeoSettingsTest {
    @Test void developmentRemainsNoindex(){
        var settings=new SeoSettings("http://localhost:8080/",false);
        assertEquals("http://localhost:8080/produtos/teste",settings.url("/produtos/teste"));
        assertEquals("noindex, nofollow",settings.robots(false));
        assertEquals("User-agent: *\nDisallow: /\n",new CrawlerController(null,settings.baseUrl(),false).robots());
    }
    @Test void productionRequiresPublicHttpsOrigin(){
        for(String base:new String[]{"http://example.com","https://localhost","https://127.0.0.1","https://example.com/path","https://u:p@example.com","https://example.com?x=1","https://example.com/#test"})
            assertThrows(IllegalArgumentException.class,()->new SeoSettings(base,true));
        var settings=new SeoSettings("https://tabaratodemais.example",true);
        assertEquals("index, follow",settings.robots(false));
        assertEquals("noindex, nofollow",settings.robots(true));
    }
}
