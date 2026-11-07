package com.tabaratodemais.offer;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public final class MarketplaceLinks {
    private MarketplaceLinks() {}
    public static URI https(String value) {
        try {
            URI uri = URI.create(value);
            if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)) throw new IllegalArgumentException();
            return uri;
        } catch (RuntimeException e) { throw new IllegalArgumentException("Use um endereço HTTPS válido, sem usuário ou porta personalizada."); }
    }
    public static void validate(String store, String value) {
        String host = https(value).getHost().toLowerCase(Locale.ROOT);
        Set<String> roots = switch(store) {
            case "mercado-livre" -> Set.of("meli.la", "mercadolivre.com.br");
            case "shopee" -> Set.of("shopee.com.br", "shope.ee");
            default -> throw new IllegalArgumentException("Selecione Mercado Livre ou Shopee.");
        };
        boolean valid = roots.stream().anyMatch(root -> host.equals(root)
            || (root.endsWith(".com.br") && host.endsWith("." + root)));
        if (!valid) throw new IllegalArgumentException("O domínio do link não corresponde à loja selecionada.");
    }
}
