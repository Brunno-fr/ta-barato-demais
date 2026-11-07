package com.tabaratodemais.offer;

import com.tabaratodemais.catalog.ProductAdminService;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PriceHistoryService {
    private static final int PAGE_SIZE = 25;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        .withZone(ProductAdminService.ZONE);
    private final OfferRepository offers;
    private final JdbcTemplate jdbc;

    public PriceHistoryService(OfferRepository offers, JdbcTemplate jdbc) {
        this.offers = offers;
        this.jdbc = jdbc;
    }

    public record Entry(BigDecimal price, String recordedAt) {}
    public record History(Long productId, String productName, String storeName,
                          List<Entry> entries, long total, int page, int pages) {}

    public History get(Long productId, Long offerId, int page) {
        if (page < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Offer offer = offers.findById(offerId)
            .filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        long total = jdbc.queryForObject("select count(*) from price_history where offer_id = ?", Long.class, offerId);
        int pages = (int) Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(page, pages);
        List<Entry> entries = jdbc.query("""
            select price, recorded_at from price_history where offer_id = ?
            order by recorded_at desc, id desc limit ? offset ?
            """, (rs, row) -> new Entry(rs.getBigDecimal("price"),
                DATE.format(rs.getTimestamp("recorded_at").toInstant())),
            offerId, PAGE_SIZE, (long) (current - 1) * PAGE_SIZE);
        return new History(productId, offer.getProduct().getName(), offer.getStore().getName(),
            entries, total, current, pages);
    }
}
