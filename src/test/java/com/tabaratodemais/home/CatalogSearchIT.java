package com.tabaratodemais.home;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.offer.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogSearchIT {
    @Autowired CatalogService catalog;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @Autowired OfferRepository offers;
    @Autowired StoreRepository stores;
    private final String token="search-"+UUID.randomUUID();

    private Category category(Category parent) {
        return categories.saveAndFlush(new Category("Categoria "+token,"cat-"+UUID.randomUUID(),parent));
    }
    private Product product(Category category,int n,ProductStatus status) {
        Product p=new Product(category,token+" Fone "+n,"prod-"+UUID.randomUUID(),"Áudio","M"+n,"Preto");
        p.update(category,p.getName(),p.getSlug(),p.getBrand(),p.getModel(),p.getVariant(),"","https://example.com/test.jpg",status);
        return products.saveAndFlush(p);
    }
    private void offer(Product p,String store,int price,boolean active) {
        Store s=stores.findBySlug(store).orElseThrow();
        String link=store.equals("shopee") ? "https://shope.ee/test" : "https://meli.la/test";
        Offer o=new Offer(p,s,BigDecimal.valueOf(price),link,Instant.now());
        o.update(s,BigDecimal.valueOf(price),link,null,null,Instant.now(),active);
        offers.saveAndFlush(o);
    }
    private CatalogService.SearchPage search(Long cat,String store,String sort,int page) {
        return catalog.search(new CatalogFilter(token,cat,store,sort,page));
    }
    @Test void paginatesProductsWithoutDuplicatesAndKeepsStablePriceOrdering() {
        Category c=category(null);
        for(int i=0;i<14;i++) {
            Product p=product(c,i,ProductStatus.PUBLISHED);
            offer(p,"mercado-livre",100+i,true);offer(p,"shopee",200+i,true);
        }
        var first=search(null,"","preco-asc",1);var second=search(null,"","preco-asc",2);
        assertEquals(14,first.total());assertEquals(2,first.pages());assertEquals(12,first.cards().size());assertEquals(2,second.cards().size());
        Set<String> slugs=new HashSet<>();first.cards().forEach(card -> assertTrue(slugs.add(card.slug())));second.cards().forEach(card -> assertTrue(slugs.add(card.slug())));
        assertEquals(2,first.cards().getFirst().deals().size());
        assertTrue(first.cards().getFirst().name().endsWith("Fone 0"));
        assertTrue(second.cards().getFirst().name().endsWith("Fone 12"));
        assertTrue(search(null,"","preco-desc",1).cards().getFirst().name().endsWith("Fone 13"));
        assertTrue(search(null,"","recentes",1).cards().getFirst().name().endsWith("Fone 13"));
        assertEquals(2,search(null,"","preco-asc",999).page());
    }
    @Test void categoryIncludesDescendantsAndStoreDeterminesPricesAndVisibleOffers() {
        Category root=category(null),child=category(root),grandchild=category(child),other=category(null);
        Product a=product(grandchild,1,ProductStatus.PUBLISHED),b=product(root,2,ProductStatus.PUBLISHED),outside=product(other,3,ProductStatus.PUBLISHED);
        offer(a,"mercado-livre",10,true);offer(a,"shopee",100,true);offer(b,"shopee",50,true);offer(outside,"shopee",1,true);
        var result=search(root.getId(),"shopee","preco-asc",1);
        assertEquals(2,result.total());assertEquals(b.getSlug(),result.cards().getFirst().slug());
        assertTrue(result.cards().stream().allMatch(c -> c.deals().size()==1 && c.deals().getFirst().store().equals("Shopee")));
        assertEquals(a.getSlug(),search(child.getId(),"mercado-livre","preco-asc",1).cards().getFirst().slug());
        assertTrue(search(Long.MAX_VALUE,"","preco-asc",1).cards().isEmpty());
    }
    @Test void searchIgnoresAccentsAndRequiresEveryTermWithoutExposingPrivateProducts() {
        Category c=category(null);Product p=product(c,7,ProductStatus.PUBLISHED);offer(p,"mercado-livre",100,true);
        offer(product(c,8,ProductStatus.DRAFT),"shopee",1,true);
        offer(product(c,9,ProductStatus.ARCHIVED),"shopee",1,true);
        offer(product(c,10,ProductStatus.PUBLISHED),"shopee",1,false);
        var result=catalog.search(new CatalogFilter(token+" AUDIO M7 PRETO",null,"","preco-asc",1));
        assertEquals(1,result.total());assertEquals(p.getSlug(),result.cards().getFirst().slug());
        assertEquals(0,catalog.search(new CatalogFilter(token+" inexistente",null,"","preco-asc",1)).total());
        assertEquals(0,catalog.search(new CatalogFilter("%' OR 1=1 --",null,"","preco-asc",1)).total());
        assertEquals(0,catalog.search(new CatalogFilter("%_",null,"","preco-asc",1)).total());
        assertEquals(1,search(null,"","preco-asc",1).total());
    }
}
