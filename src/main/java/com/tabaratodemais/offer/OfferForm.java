package com.tabaratodemais.offer;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class OfferForm {
    @NotBlank(message="Selecione a loja.") public String storeSlug;
    @NotNull(message="Informe o preço.") @DecimalMin("0.01") @Digits(integer=10,fraction=2) public BigDecimal price;
    @NotBlank(message="Cole o link de afiliado.") @Size(max=2048) public String affiliateUrl;
    @DecimalMin("0.00") @Digits(integer=10,fraction=2) public BigDecimal shippingCost;
    @NotBlank @Pattern(regexp="UNKNOWN|FREE|PAID", message="Selecione uma opção de frete válida.")
    public String shippingType = "UNKNOWN";
    @Size(max=2000) public String purchaseConditions;
    @NotNull(message="Informe quando conferiu a oferta.")
    @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") public LocalDateTime checkedAt;
    public boolean active;
    public boolean featured;
    public boolean isFeatured(){return featured;} public void setFeatured(boolean v){featured=v;}
    public Long version;
    public String getStoreSlug(){return storeSlug;} public void setStoreSlug(String v){storeSlug=v;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
    public String getAffiliateUrl(){return affiliateUrl;} public void setAffiliateUrl(String v){affiliateUrl=v == null ? null : v.strip();}
    public BigDecimal getShippingCost(){return shippingCost;} public void setShippingCost(BigDecimal v){shippingCost=v;}
    public String getShippingType(){return shippingType;} public void setShippingType(String v){shippingType=v;}
    public String getPurchaseConditions(){return purchaseConditions;} public void setPurchaseConditions(String v){purchaseConditions=v;}
    public LocalDateTime getCheckedAt(){return checkedAt;} public void setCheckedAt(LocalDateTime v){checkedAt=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public Long getVersion(){return version;} public void setVersion(Long v){version=v;}
}
