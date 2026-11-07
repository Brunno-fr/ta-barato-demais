package com.tabaratodemais.offer;

import com.tabaratodemais.catalog.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OfferTest {
    private final Product product = new Product(new Category("Teste", "teste", null),
        "Produto sintético de teste", "produto-teste", "Teste", "M1", "V1");
    private final Store store = new Store("Loja de teste", "teste");
    @Test void rejectsNonPositivePriceAndInsecureLink() {
        assertThatThrownBy(() -> new Offer(product, store, BigDecimal.ZERO,
            "https://example.com/test", Instant.now())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Offer(product, store, BigDecimal.ONE,
            "javascript:alert(1)", Instant.now())).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejectsSilentRoundingAndStartsInactiveWithoutInventedReferencePrice() {
        assertThatThrownBy(() -> new Offer(product, store, new BigDecimal("1.999"),
            "https://example.com/test", Instant.now())).isInstanceOf(ArithmeticException.class);
        var offer = new Offer(product, store, new BigDecimal("10.00"),
            "https://example.com/test", Instant.now());
        assertThat(offer.getActive()).isFalse();
        assertThat(offer.getPreviousPrice()).isNull();
        assertThat(offer.getShippingCost()).isNull();
    }
}
