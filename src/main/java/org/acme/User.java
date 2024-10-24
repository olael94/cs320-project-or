package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;


@Entity
@Table(name = "User")
public class User extends PanacheEntity {

    //Since we're using PanacheEntity, the id field is provided automatically, so no need to define userID manually.

    @Column(nullable = false)
    public String username;

    @Column(nullable = false)
    public String email;

    @Column(nullable = false)
    public String password;

    @Enumerated(EnumType.STRING)
    public Role role;

    public enum Role {
        customer,
        admin,
        vendor,
    }
}
