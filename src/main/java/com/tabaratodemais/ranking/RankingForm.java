package com.tabaratodemais.ranking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class RankingForm {
    @NotBlank @Size(max=160) public String title;
    @NotBlank @Size(max=180) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") public String slug;
    @Size(max=2000) public String description="";
    @NotBlank @Size(max=3000) public String criteria;
    public boolean published;
    @Min(0) public Long version;
    @Valid @NotNull @Size(min=10,max=10) public List<Item> items=new ArrayList<>();
    public RankingForm(){for(int i=0;i<10;i++) items.add(new Item());}
    public static class Item {
        @Positive public Long productId;
        @Size(max=1000) public String reason="";
        public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
        public String getReason(){return reason;} public void setReason(String v){reason=v==null ? "" : v.strip();}
    }
    public String getTitle(){return title;} public void setTitle(String v){title=v==null ? null : v.strip();}
    public String getSlug(){return slug;} public void setSlug(String v){slug=v==null ? null : v.strip();}
    public String getDescription(){return description;} public void setDescription(String v){description=v==null ? "" : v.strip();}
    public String getCriteria(){return criteria;} public void setCriteria(String v){criteria=v==null ? null : v.strip();}
    public boolean isPublished(){return published;} public void setPublished(boolean v){published=v;}
    public Long getVersion(){return version;} public void setVersion(Long v){version=v;}
    public List<Item> getItems(){return items;} public void setItems(List<Item> v){items=v;}
}
