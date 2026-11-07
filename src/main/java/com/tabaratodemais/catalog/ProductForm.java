package com.tabaratodemais.catalog;
import jakarta.validation.constraints.*;

public class ProductForm {
    @NotNull(message="Selecione a categoria.") public Long categoryId;
    @NotBlank(message="Informe o nome.") @Size(max=200) public String name;
    @NotBlank @Size(max=220) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*", message="URL amigável: use letras minúsculas sem acentos, números e hífens.") public String slug;
    @NotBlank(message="Informe a marca.") @Size(max=100) public String brand;
    @NotBlank(message="Informe o modelo exato.") @Size(max=150) public String model;
    @NotBlank(message="Informe a versão: memória, cor, tamanho ou outra característica.") @Size(max=150) public String variant;
    @Size(max=5000) public String description = "";
    @NotBlank(message="Informe o endereço HTTPS da imagem.") @Size(max=2048) public String imageUrl;
    @NotNull public ProductStatus status = ProductStatus.DRAFT;
    public Long getCategoryId(){return categoryId;} public void setCategoryId(Long v){categoryId=v;}
    public String getName(){return name;} public void setName(String v){name=v == null ? null : v.strip();}
    public String getSlug(){return slug;} public void setSlug(String v){slug=v == null ? null : v.strip();}
    public String getBrand(){return brand;} public void setBrand(String v){brand=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;}
    public String getVariant(){return variant;} public void setVariant(String v){variant=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v == null ? null : v.strip();}
    public ProductStatus getStatus(){return status;} public void setStatus(ProductStatus v){status=v;}
}
