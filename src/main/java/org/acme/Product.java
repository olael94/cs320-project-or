package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Product")
public class Product extends PanacheEntity {
    @JsonProperty("productName")
    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String imageURL;

    @Column(nullable = false)
    private Integer quantity;

    // Getters
    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Integer getQuantity() {
        return quantity;
    }

    // Setters
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // The toString method is used to convert the object to a string representation.
    @Override
    public String toString() {
        return productName + " " + description + " " + price + " " + imageURL;
    }

}
