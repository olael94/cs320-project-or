package org.acme.dto;

import org.acme.entity.Product;

// Lightweight product representation for list views.
public class ProductSummaryDto {
  public Long id;
  public String productName;
  public Double price;
  public String imageURL;
  public Integer quantity;

  public ProductSummaryDto(Product product) {
    this.id = product.id;
    this.productName = product.getProductName();
    this.price = product.getPrice();
    this.imageURL = product.getImageURL();
    this.quantity = product.getQuantity();
  }
}
