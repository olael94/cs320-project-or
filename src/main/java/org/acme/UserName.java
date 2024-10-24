package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_names")
public class UserName extends PanacheEntity {
    public String name;

    // Getter for the name field
    public UserName() {
    }
    // Setter for the name field
    public UserName(String name) {
        this.name = name;
    }

    // The toString method is used to convert the object to a string representation.
    @Override
    public String toString() {
        return name;
    }
}
