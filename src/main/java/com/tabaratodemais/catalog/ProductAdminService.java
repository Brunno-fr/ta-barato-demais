package com.tabaratodemais.catalog;

import com.tabaratodemais.offer.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class ProductAdminService {
    public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final OfferRepository offers;
    private final StoreRepository stores;
    private final JdbcTemplate jdbc;
    public ProductAdminService(ProductRepository products, CategoryRepository categories, OfferRepository offers, StoreRepository stores, JdbcTemplate jdbc) {
        this.products=products; this.categories=categories; this.offers=offers; this.stores=stores; this.jdbc=jdbc;
    }
    public List<Product> list(){ return products.findAllWithCategory(); }
    public Product product(Long id){ return products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }
    public List<Offer> offers(Long id){ product(id); return offers.findForProduct(id); }
    public ProductForm form(Long id) {
        Product p=product(id); var f=new ProductForm(); f.categoryId=p.getCategory().getId();
        f.name=p.getName(); f.slug=p.getSlug(); f.brand=p.getBrand(); f.model=p.getModel(); f.variant=p.getVariant();
        f.description=p.getDescription(); f.imageUrl=p.getImageUrl(); f.status=p.getStatus(); return f;
    }
    @Transactional
    public Long save(Long id, ProductForm f) {
        jdbc.execute("SELECT pg_advisory_xact_lock(84201032)");
        MarketplaceLinks.https(f.imageUrl);
        products.findBySlug(f.slug).ifPresent(p -> { if(!Objects.equals(p.getId(),id)) throw new IllegalArgumentException("Esta URL amigável já está em uso."); });
        Category c=categories.findById(f.categoryId).orElseThrow(() -> new IllegalArgumentException("Selecione uma categoria existente."));
        if(f.status==ProductStatus.PUBLISHED && (id==null || offers.findForProduct(id).stream().noneMatch(Offer::getActive)))
            throw new IllegalArgumentException("Salve como rascunho, cadastre uma oferta ativa e depois publique o produto.");
        Product p=id==null ? new Product(c,f.name,f.slug,f.brand,f.model,f.variant) : product(id);
        p.update(c,f.name,f.slug,f.brand,f.model,f.variant, f.description==null ? "" : f.description,f.imageUrl,f.status);
        return products.saveAndFlush(p).getId();
    }
    private Offer offer(Long productId, Long offerId) {
        return offers.findById(offerId).filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    public OfferForm offerForm(Long productId, Long offerId) {
        Offer o=offer(productId,offerId); var f=new OfferForm(); f.storeSlug=o.getStore().getSlug(); f.price=o.getPrice();
        f.affiliateUrl=o.getAffiliateUrl(); f.shippingCost=o.getShippingCost(); f.purchaseConditions=o.getPurchaseConditions();
        f.shippingType=o.getShippingCost()==null ? "UNKNOWN" : (o.getShippingCost().signum()==0 ? "FREE" : "PAID");
        f.checkedAt=LocalDateTime.ofInstant(o.getLastCheckedAt(),ZONE); f.active=o.getActive(); f.featured=o.getFeatured(); f.version=o.getVersion(); return f;
    }
    @Transactional
    public void saveOffer(Long productId, Long offerId, OfferForm f) {
        jdbc.execute("SELECT pg_advisory_xact_lock(84201032)");
        Product p=product(productId);
        MarketplaceLinks.validate(f.storeSlug,f.affiliateUrl);
        Instant checked=f.checkedAt.atZone(ZONE).toInstant();
        if(checked.isAfter(Instant.now())) throw new IllegalArgumentException("A data de verificação não pode estar no futuro (horário de Brasília).");
        Store store=stores.findBySlug(f.storeSlug).orElseThrow(() -> new IllegalArgumentException("Loja não cadastrada."));
        Offer o=offerId==null ? new Offer(p,store,f.price,f.affiliateUrl,checked) : offer(productId,offerId);
        if(offerId!=null && !Objects.equals(f.version,o.getVersion())) throw new IllegalArgumentException("Esta oferta mudou em outra janela. Reabra a edição antes de salvar.");
        java.math.BigDecimal shipping = switch (f.shippingType) {
            case "UNKNOWN" -> null;
            case "FREE" -> java.math.BigDecimal.ZERO;
            case "PAID" -> {
                if (f.shippingCost==null || f.shippingCost.signum()<=0)
                    throw new IllegalArgumentException("Informe o valor do frete pago ou selecione Frete grátis.");
                yield f.shippingCost;
            }
            default -> throw new IllegalArgumentException("Selecione uma opção de frete válida.");
        };
        o.update(store,f.price,f.affiliateUrl,shipping,f.purchaseConditions,checked,f.active);
        o.setFeatured(f.featured);
        offers.saveAndFlush(o);
    }
}
