package com.tabaratodemais.seo;

import java.net.URI;

public record SeoSettings(String baseUrl, boolean indexingEnabled) {
    public SeoSettings {
        URI uri=URI.create(baseUrl);
        if(uri.getHost()==null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null
            || !(uri.getScheme().equals("https") || uri.getScheme().equals("http"))
            || !(uri.getPath().isEmpty() || uri.getPath().equals("/")))
            throw new IllegalArgumentException("SITE_BASE_URL deve conter somente a origem do site, sem caminho, credenciais ou parâmetros.");
        if(indexingEnabled && (!uri.getScheme().equals("https") || uri.getHost().equalsIgnoreCase("localhost")
            || uri.getHost().equals("127.0.0.1") || uri.getHost().contains(":")))
            throw new IllegalArgumentException("Para liberar indexação, configure SITE_BASE_URL com o domínio público HTTPS.");
        baseUrl=baseUrl.replaceAll("/+$","");
    }
    public String url(String path){return baseUrl+path;}
    public String robots(boolean filtered){return indexingEnabled && !filtered ? "index, follow" : "noindex, nofollow";}
}
