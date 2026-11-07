package com.tabaratodemais.seo;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.HtmlUtils;

@RestController
public class CrawlerController {
    private static final int PAGE_SIZE=1000;
    private static final String URLS="""
        select '/' as path union all select '/rankings'
        union all select '/produtos/' || slug from products where status='PUBLISHED'
        union all select '/rankings/' || slug from rankings where published=true
        """;
    private final JdbcTemplate jdbc;
    private final SeoSettings settings;
    public CrawlerController(JdbcTemplate jdbc,@Value("${site.base-url:http://localhost:8080}") String base,
                             @Value("${site.indexing-enabled:false}") boolean enabled){this.jdbc=jdbc;settings=new SeoSettings(base,enabled);}
    @GetMapping(value="/robots.txt",produces="text/plain;charset=UTF-8")
    public String robots(){
        return settings.indexingEnabled() ? "User-agent: *\nDisallow: /admin\nSitemap: "+settings.url("/sitemap.xml")+"\n" : "User-agent: *\nDisallow: /\n";
    }
    @GetMapping(value="/sitemap.xml",produces="application/xml;charset=UTF-8")
    public String index(){
        long total=jdbc.queryForObject("select count(*) from ("+URLS+") urls",Long.class);
        long pages=(total+PAGE_SIZE-1)/PAGE_SIZE;
        StringBuilder xml=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for(long page=1;page<=pages;page++) xml.append("<sitemap><loc>").append(escape(settings.url("/sitemaps/"+page+".xml"))).append("</loc></sitemap>");
        return xml.append("</sitemapindex>").toString();
    }
    @GetMapping(value="/sitemaps/{page}.xml",produces="application/xml;charset=UTF-8")
    public String page(@PathVariable int page){
        if(page<1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<String> paths=jdbc.queryForList("select path from ("+URLS+") urls order by path limit ? offset ?",String.class,PAGE_SIZE,(long)(page-1)*PAGE_SIZE);
        if(paths.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        StringBuilder xml=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for(String path:paths) xml.append("<url><loc>").append(escape(settings.url(path))).append("</loc></url>");
        return xml.append("</urlset>").toString();
    }
    private String escape(String value){return HtmlUtils.htmlEscape(value,"UTF-8");}
}
