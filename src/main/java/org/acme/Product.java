package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Product")
public class Product extends PanacheEntity {
    @JsonProperty("productName")
    @Column(nullable = false)
    public String productName;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public Double price;

    @Column(nullable = false)
    public String imageURL;
}
