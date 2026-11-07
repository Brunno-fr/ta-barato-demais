package com.tabaratodemais.offer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarketplaceLinksTest {
    @Test void acceptsOfficialLinksAndPreservesAffiliateParameters() {
        for(String link : new String[]{"https://meli.la/18RdYrP", "https://produto.mercadolivre.com.br/MLB-123?tracking_id=abc%2F123"})
            assertDoesNotThrow(() -> MarketplaceLinks.validate("mercado-livre",link));
        for(String link : new String[]{"https://shope.ee/abc", "https://s.shopee.com.br/abc", "https://shopee.com.br/product/1/2?utm_source=affiliate"})
            assertDoesNotThrow(() -> MarketplaceLinks.validate("shopee",link));
    }
    @Test void rejectsWrongStoreSpoofedHostsAndUnsafeSchemes() {
        for(String link : new String[]{"javascript:alert(1)","http://meli.la/test","https://meli.la.evil.test/test",
                "https://evilmercadolivre.com.br/test", "https://meli.la@evil.test/test", "https://meli.la:444/test",
                "https://shopee.com.br/test", "https://localhost/test", "//meli.la/test"})
            assertThrows(IllegalArgumentException.class,() -> MarketplaceLinks.validate("mercado-livre",link));
    }
}
