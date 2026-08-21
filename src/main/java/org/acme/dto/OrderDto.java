package org.acme.dto;

import java.time.LocalDateTime;
import org.acme.entity.Order;

public class OrderDto {
  public Long id;
  public LocalDateTime orderDate;
  public Double totalAmount;
  public String status;
  public String username;
  public String guestEmail;
  public String guestTrackingId;

  public OrderDto(Order order) {
    this.id = order.id;
    this.orderDate = order.getOrderDate();
    this.totalAmount = order.getTotalAmount();

    if (order.getStatus() != null) {
      this.status = order.getStatus().toString();
    }

    if (order.getUser() != null) {
      this.username = order.getUser().getUsername();
    }

    this.guestEmail = order.getGuestEmail();
    this.guestTrackingId = order.getGuestTrackingId();
  }
}
