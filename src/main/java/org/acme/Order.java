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
    private User user;                                               // Will be null for guest orders

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = true)                                        // GUEST ORDERS Store guest email for guest orders
    private String guestEmail;

    @Column(nullable = true, unique = true)                         // GUEST ORDERS Unique guest tracking ID
    private String guestTrackingId;

    private enum Status {
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

    // Getters
    public User getUser() {
        return user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Status getStatus() {
        return status;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public String getGuestTrackingId() {
        return guestTrackingId;
    }

    // Setters
    public void setUser(User user) {
        this.user = user;   // this keyword refers to the current object
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public void setGuestTrackingId(String guestTrackingId) {
        this.guestTrackingId = guestTrackingId;
    }

    // The toString method is used to convert the object to a string representation.
    @Override
    public String toString() {
        return user + " " + orderDate + " " + totalAmount + " " + status + " " + guestEmail + " " + guestTrackingId;
    }

}
