package com.tabaratodemais.ranking;

import com.tabaratodemais.catalog.*;
import com.tabaratodemais.home.CatalogService;
import java.util.*;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional(readOnly=true)
public class RankingService {
    private final JdbcTemplate jdbc;
    private final ProductRepository products;
    private final CatalogService catalog;
    public RankingService(JdbcTemplate jdbc,ProductRepository products,CatalogService catalog){this.jdbc=jdbc;this.products=products;this.catalog=catalog;}
    public record Row(Long id,String title,String slug,String description,String criteria,boolean published,long version,String updatedAt) {}
    public record Entry(int position,Long productId,String reason) {}
    public record PublicItem(int position,String reason,String slug,CatalogService.Detail product) {}
    public record Page(Row ranking,List<PublicItem> items) {}
    private static final RowMapper<Row> MAPPER=(r,n) -> new Row(r.getLong("id"),r.getString("title"),r.getString("slug"),r.getString("description"),r.getString("criteria"),r.getBoolean("published"),r.getLong("version"),DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ProductAdminService.ZONE).format(r.getTimestamp("updated_at").toInstant()));
    public List<Row> list(boolean publicOnly){return jdbc.query("select * from rankings"+(publicOnly ? " where published=true" : "")+" order by updated_at desc,id desc",MAPPER);}
    private Row get(Long id){return jdbc.query("select * from rankings where id=?",MAPPER,id).stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));}
    private List<Entry> entries(Long id){return jdbc.query("select * from ranking_items where ranking_id=? order by position",(r,n)->new Entry(r.getInt("position"),r.getLong("product_id"),r.getString("reason")),id);}
    public RankingForm form(Long id){
        var row=get(id);var f=new RankingForm();f.title=row.title();f.slug=row.slug();f.description=row.description();f.criteria=row.criteria();f.published=row.published();f.version=row.version();
        for(var entry:entries(id)){var item=f.items.get(entry.position()-1);item.productId=entry.productId();item.reason=entry.reason();}return f;
    }
    @Transactional
    public Long save(Long id,RankingForm f){
        jdbc.execute("select pg_advisory_xact_lock(84201033)");
        if(id!=null && !Objects.equals(get(id).version(),f.version)) throw new IllegalArgumentException("Este ranking mudou em outra janela. Reabra a edição.");
        if(jdbc.queryForObject("select count(*) from rankings where slug=? and id<>coalesce(cast(? as bigint),0)",Long.class,f.slug,id)>0)
            throw new IllegalArgumentException("Esta URL amigável já está em uso.");
        Set<Long> selected=new HashSet<>();
        for(var item:f.items){
            if(item.productId==null){if(item.reason!=null && !item.reason.isBlank()) throw new IllegalArgumentException("Selecione o produto da justificativa preenchida.");continue;}
            if(!selected.add(item.productId)) throw new IllegalArgumentException("Selecione cada produto apenas uma vez.");
            if(item.reason==null || item.reason.isBlank()) throw new IllegalArgumentException("Explique o motivo da indicação de cada produto.");
            var p=products.findById(item.productId).orElseThrow(()->new IllegalArgumentException("Selecione um produto cadastrado."));
            if(f.published && p.getStatus()!=ProductStatus.PUBLISHED) throw new IllegalArgumentException("Para publicar o ranking, todos os produtos selecionados devem estar publicados.");
        }
        if(f.published && selected.size()<2) throw new IllegalArgumentException("Selecione pelo menos dois produtos para publicar o ranking. Você pode salvar como rascunho.");
        if(id==null) id=jdbc.queryForObject("insert into rankings(title,slug,description,criteria,published) values (?,?,?,?,?) returning id",Long.class,f.title,f.slug,f.description,f.criteria,f.published);
        else jdbc.update("update rankings set title=?,slug=?,description=?,criteria=?,published=?,version=version+1,updated_at=current_timestamp where id=?",f.title,f.slug,f.description,f.criteria,f.published,id);
        jdbc.update("delete from ranking_items where ranking_id=?",id);
        int position=0;
        for(var item:f.items){position++;if(item.productId!=null) jdbc.update("insert into ranking_items(ranking_id,product_id,position,reason) values (?,?,?,?)",id,item.productId,position,item.reason);}
        return id;
    }
    public Page publicPage(String slug){
        Row row=jdbc.query("select * from rankings where slug=? and published=true",MAPPER,slug).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<PublicItem> items=new ArrayList<>();
        for(var entry:entries(row.id())) products.findById(entry.productId()).filter(p->p.getStatus()==ProductStatus.PUBLISHED).ifPresent(p->items.add(new PublicItem(entry.position(),entry.reason(),p.getSlug(),catalog.detail(p.getSlug()))));
        return new Page(row,items);
    }
}
