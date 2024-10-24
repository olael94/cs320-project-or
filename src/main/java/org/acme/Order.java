package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "`Order`") // Backticks to avoid reserved word issues
public class Order extends PanacheEntity {

    @ManyToOne(optional = true)                                     // Optional to allow for orders without users
    @JoinColumn(name = "fk_user_id")
    public User user;                                               // Will be null for guest orders

    @Column(nullable = false)
    public LocalDateTime orderDate;

    @Column(nullable = false)
    public Double totalAmount;

    @Enumerated(EnumType.STRING)
    public Status status;

    @Column(nullable = true)                                        // GUEST ORDERS Store guest email for guest orders
    public String guestEmail;

    @Column(nullable = true, unique = true)                         // GUEST ORDERS Unique guest tracking ID
    public String guestTrackingId;

    public enum Status {
        pending,
        completed,
        canceled,
        refunded // Add other statuses as needed
    }

    // Generate a guestTrackingId if the order is a guest order and doesn't have an existing one
    @PrePersist
    public void generateGuestTrackingId() {
        if (user == null && guestTrackingId == null) {
            guestTrackingId = UUID.randomUUID().toString();
        }
    }

}
