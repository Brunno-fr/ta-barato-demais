package com.tabaratodemais.seo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SeoAdvice {
    private final SeoSettings settings;
    public SeoAdvice(@Value("${site.base-url:http://localhost:8080}") String base,
                     @Value("${site.indexing-enabled:false}") boolean enabled){settings=new SeoSettings(base,enabled);}
    public record Metadata(String canonical,String robots,String image) {}
    @ModelAttribute("seo") public Metadata metadata(HttpServletRequest request){
        return new Metadata(settings.url(request.getRequestURI()),
            settings.robots(!request.getParameterMap().isEmpty() || request.getRequestURI().startsWith("/admin")),settings.url("/images/brand.png"));
    }
}
