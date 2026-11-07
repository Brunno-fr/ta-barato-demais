package com.tabaratodemais.home;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.offer.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final OfferRepository offers;
    private final ProductRepository products;
    private final CatalogSearchRepository search;
    public CatalogService(OfferRepository offers, ProductRepository products, CatalogSearchRepository search){this.offers=offers;this.products=products;this.search=search;}
    public record SearchPage(List<Card> cards, long total, int page, int pages) {}
    public record Deal(String store, String url, String price, String shipping, String conditions, String checkedAt) {}
    public record Card(String name, String slug, String image, String category, String groups, String details, List<Deal> deals) {}
    public record Detail(String name, String image, String category, String brand, String model,
                         String variant, String description, List<Deal> deals) {}

    @Transactional(readOnly=true)
    public List<Card> featured() {
        return offers.findFeatured(org.springframework.data.domain.PageRequest.of(0,6)).stream().map(o -> {
            Product p=o.getProduct();
            return new Card(p.getName(),p.getSlug(),p.getImageUrl(),p.getCategory().getName(),"",
                p.getBrand()+" · "+p.getModel()+" · "+p.getVariant(),List.of(deal(o)));
        }).toList();
    }

    @Transactional(readOnly=true)
    public Detail detail(String slug) {
        Product p=products.findBySlug(slug).filter(product -> product.getStatus()==ProductStatus.PUBLISHED)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        var deals=offers.findForProduct(p.getId()).stream().filter(Offer::getActive).map(this::deal).toList();
        return new Detail(p.getName(),p.getImageUrl(),p.getCategory().getName(),p.getBrand(),p.getModel(),
            p.getVariant(),p.getDescription(),deals);
    }

    private Deal deal(Offer o) {
        var money=NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        var date=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ProductAdminService.ZONE);
        return new Deal(o.getStore().getName(),o.getAffiliateUrl(),money.format(o.getPrice()),
            o.getShippingCost()==null ? "Frete não informado" : (o.getShippingCost().signum()==0 ? "Frete grátis" : "Frete: "+money.format(o.getShippingCost())),
            o.getPurchaseConditions(),date.format(o.getLastCheckedAt())+" (Brasília)");
    }
    @Transactional(readOnly=true)
    public SearchPage search(CatalogFilter filter) {
        var result=search.search(filter);
        if(result.ids().isEmpty()) return new SearchPage(List.of(),result.total(),result.page(),result.pages());
        Map<Product,List<Offer>> grouped=new LinkedHashMap<>();
        for(Offer o:offers.findPublicForProducts(result.ids())) {
            if(filter.store().isEmpty() || filter.store().equals(o.getStore().getSlug()))
                grouped.computeIfAbsent(o.getProduct(), p -> new ArrayList<>()).add(o);
        }
        Map<Long,Card> cards=new HashMap<>();
        grouped.forEach((p, rows) -> {
            List<String> ids=new ArrayList<>();
            for(Category c=p.getCategory();c!=null && !ids.contains(c.getId().toString());c=c.getParent()) ids.add(c.getId().toString());
            var deals=rows.stream().map(this::deal).toList();
            cards.put(p.getId(),new Card(p.getName(),p.getSlug(),p.getImageUrl(),p.getCategory().getName(),String.join(" ",ids),
                p.getBrand()+" · "+p.getModel()+" · "+p.getVariant(),deals));
        });
        return new SearchPage(result.ids().stream().map(cards::get).filter(Objects::nonNull).toList(),result.total(),result.page(),result.pages());
    }
}
