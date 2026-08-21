package org.acme.controller;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.acme.dto.MessageDto;
import org.acme.dto.OrderDto;
import org.acme.entity.Order;
import org.acme.entity.Session;
import org.acme.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/orders")
@Produces("application/json")
@Consumes("application/json")
public class OrderController {

  // The logger object is used to log messages to the console.
  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

  // Create a new order (guest or user)
  @POST
  @Transactional
  public Response createOrder(Order order) {
    logger.info("Received request to create an order");

    if (order.getUser() != null) {
      // Fetch and validate the user if provided
      User user = User.findById(order.getUser().id);
      if (user == null) {
        logger.warn("User not found for ID: {}", order.getUser().id);
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new MessageDto("User not found")) // User will see this message
            .build();
      }
      order.setUser(user);
    }

    order.setOrderDate(LocalDateTime.now());
    // The guestTrackingId will be generated automatically by the @PrePersist method

    order.persist(); // Persist the order to the database

    // User will see this message
    String trackingInfo =
        order.getUser() == null
            ? "Your order guest tracking number is " + order.getGuestTrackingId()
            : "Your order tracking number is " + order.id;

    logger.info("Order created successfully with tracking info: {}", trackingInfo);

    return Response.status(Response.Status.CREATED).entity(new MessageDto(trackingInfo)).build();
  }

  // Get all orders (admin only)
  @GET
  public Response getAllOrders(@CookieParam("session") Cookie sessionCookie) {
    logger.info("Fetching all orders");

    if (sessionCookie == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Session session = Session.findValid(sessionCookie.getValue());
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    // Only admins may view all orders
    if (!session.user.hasRole(User.Role.ADMIN)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    logger.info("All orders fetched successfully");
    List<Order> orders = Order.listAll();
    List<OrderDto> orderDtos = new ArrayList<>();
    for (Order order : orders) {
      orderDtos.add(new OrderDto(order));
    }

    return Response.ok(orderDtos).build();
  }

  // Get GUEST order by guestTrackingId
  @GET
  @Path("{guestTrackingId}")
  public Response getGuestOrderByTrackingId(@PathParam("guestTrackingId") String guestTrackingId) {
    logger.info("Fetching order for guestTrackingId: {}", guestTrackingId);
    Order order = Order.find("guestTrackingId", guestTrackingId).firstResult();
    if (order == null) {
      logger.warn("Order not found for guestTrackingId: {}", guestTrackingId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new MessageDto("Order not found")) // User will see this message
          .build();
    }

    logger.info("Order found for guestTrackingId: {}", guestTrackingId);
    // User will see this message
    String message = "Order found for guestTrackingId: " + order.getGuestTrackingId();
    return Response.ok(new MessageDto(message)).build();
  }

  // Get a specific USER order by ID
  @GET
  @Path("{id}")
  public Response getOrder(@PathParam("id") Long id, @CookieParam("session") Cookie sessionCookie) {
    logger.info("Fetching order for ID: {}", id);

    if (sessionCookie == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Session session = Session.findValid(sessionCookie.getValue());
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Order order = Order.findById(id);
    if (order == null) {
      logger.warn("Order not found for ID: {}", id);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new MessageDto("Order not found")) // User will see this message
          .build();
    }

    boolean isOwner = false;
    if (order.getUser() != null) {
      Long orderOwnerId = order.getUser().id;
      if (orderOwnerId.equals(session.user.id)) {
        isOwner = true;
      }
    }

    boolean isAdmin = session.user.hasRole(User.Role.ADMIN);
    boolean allowedToViewThisOrder = isOwner || isAdmin;

    if (!allowedToViewThisOrder) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    logger.info("Order found for ID: {}", id);
    // User will see this message
    String message = "Order found for ID: " + order.id;
    return Response.ok(new MessageDto(message)).build();
  }

  // Update an existing USER order by ID
  @PUT
  @Path("{id}")
  @Transactional
  public Response updateOrder(
      @PathParam("id") Long id, @CookieParam("session") Cookie sessionCookie, Order updatedOrder) {
    logger.info("Updating order with ID: {}", id);

    if (sessionCookie == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    // Find the session by token
    Session session = Session.findValid(sessionCookie.getValue());
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    // Only admins may update another user's order'
    if (!session.user.hasRole(User.Role.ADMIN)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    Order existingOrder = Order.findById(id);
    if (existingOrder == null) {
      logger.warn("Order not found for ID: {}", id);

      return Response.status(Response.Status.NOT_FOUND)
          .entity(new MessageDto("Order not found")) // User will see this message
          .build();
    }

    // Check for user existence before updating
    if (updatedOrder.getUser() != null && updatedOrder.getUser().id != null) {
      User user = User.findById(updatedOrder.getUser().id);
      if (user == null) {
        logger.warn("User not found for ID: {}", updatedOrder.getUser().id);
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new MessageDto("User not found")) // User will see this message
            .build();
      }
      existingOrder.setUser(user);
    }

    // Update order fields
    existingOrder.setOrderDate(updatedOrder.getOrderDate());
    existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
    existingOrder.setStatus(updatedOrder.getStatus());
    existingOrder.persist();

    logger.info("Order updated successfully for ID: {}", id);
    // User will see this message
    String message = "Order updated successfully for ID: " + existingOrder.id;
    return Response.ok(new MessageDto(message)).build();
  }

  // Update an existing GUEST order by guestTrackingId
  @PUT
  @Path("{guestTrackingId}")
  @Transactional
  public Response updateGuestOrder(
      @PathParam("guestTrackingId") String guestTrackingId, Order updatedGuestOrder) {
    logger.info("Updating guest order with tracking ID: {}", guestTrackingId);

    // Find the existing guest order by guestTrackingId
    Order existingGuestOrder = Order.find("guestTrackingId", guestTrackingId).firstResult();

    if (existingGuestOrder == null) {
      logger.warn("Order not found for guestTrackingId: {}", guestTrackingId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new MessageDto("Order not found")) // User will see this message
          .build();
    }

    // Update order fields
    existingGuestOrder.setOrderDate(updatedGuestOrder.getOrderDate());
    existingGuestOrder.setTotalAmount(updatedGuestOrder.getTotalAmount());
    existingGuestOrder.setStatus(updatedGuestOrder.getStatus());

    // Persist or update the order
    existingGuestOrder.persist();

    logger.info("Guest order updated successfully for guestTrackingId: {}", guestTrackingId);
    // User will see this message
    String message =
        "Guest order updated successfully for guestTrackingId: "
            + existingGuestOrder.getGuestTrackingId();
    return Response.ok(new MessageDto(message)).build();
  }

  // Delete an order by ID
  @DELETE
  @Path("{id}")
  @Transactional
  public Response deleteOrder(
      @PathParam("id") Long id, @CookieParam("session") Cookie sessionCookie) {
    logger.info("Deleting order with ID: {}", id);

    if (sessionCookie == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Session session = Session.findValid(sessionCookie.getValue());
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    // Only admins may delete another user's order'
    if (!session.user.hasRole(User.Role.ADMIN)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    Order order = Order.findById(id);
    if (order == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new MessageDto("Order not found")) // User will see this message
          .build();
    }
    order.delete();
    logger.info("Order deleted successfully for ID: {}", id);
    // User will see this message
    String message = "Order deleted successfully for ID: " + id;
    return Response.ok(new MessageDto(message)).build(); // Return 204 No Content
  }

  // Delete an order by guestTrackingId
  @DELETE
  @Path("{guestTrackingId}")
  @Transactional
  public Response deleteGuestOrder(@PathParam("guestTrackingId") String guestTrackingId) {
    logger.info("Deleting guest order with tracking ID: {}", guestTrackingId);

    Order order = Order.find("guestTrackingId", guestTrackingId).firstResult();
    if (order == null) {
      logger.warn("Order not found for guestTrackingId: {}", guestTrackingId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new MessageDto("Order not found")) // User will see this message
          .build();
    }
    order.delete();

    logger.info("Guest order deleted successfully for guestTrackingID: {}", guestTrackingId);
    // User will see this message
    String message = "Guest order deleted successfully for guestTrackingID: " + guestTrackingId;
    return Response.noContent().build(); // Return 204 No Content
  }
}
