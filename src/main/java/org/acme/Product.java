package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Product")
public class Product extends PanacheEntity {
    @Column(nullable = false)
    public String productName;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public Double price;

    @Column(nullable = false)
    public String imageURL;
}
