package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Path("/orders")
@Produces("application/json")
@Consumes("application/json")
public class OrderResource {

    //The logger object is used to log messages to the console.
    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);

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
                        .entity("User not found") // User will see this message
                        .build();
            }
            order.setUser(user);
        }

        order.setOrderDate(LocalDateTime.now());
        // The guestTrackingId will be generated automatically by the @PrePersist method

        order.persist(); // Persist the order to the database

        // User will see this message
        String trackingInfo = order.getUser() == null
                ? "Your order guest tracking number is " + order.getGuestTrackingId()
                : "Your order tracking number is " + order.id;

        logger.info("Order created successfully with tracking info: {}", trackingInfo);

        return Response.status(Response.Status.CREATED)
                .entity(trackingInfo)
                .build();
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
                    .entity("Order not found")  // User will see this message
                    .build();
        }

        logger.info("Order found for guestTrackingId: {}", guestTrackingId);
        // User will see this message
        String message = "Order found for guestTrackingId: " + order.getGuestTrackingId();
        return Response.ok(message).build();
    }

    // Get a specific USER order by ID
    @GET
    @Path("{id}")
    public Response getOrder(@PathParam("id") Long id) {
        logger.info("Fetching order for ID: {}", id);

        Order order = Order.findById(id);
        if (order == null) {
            logger.warn("Order not found for ID: {}", id);

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")  // User will see this message
                    .build();
        }
        logger.info("Order found for ID: {}", id);
        // User will see this message
        String message = "Order found for ID: " + order.id;
        return Response.ok(message).build();
    }

    // Update an existing USER order by ID
    @PUT
    @Path("{id}")
    @Transactional
    public Response updateOrder(@PathParam("id") Long id, Order updatedOrder) {
        logger.info("Updating order with ID: {}", id);

        Order existingOrder = Order.findById(id);
        if (existingOrder == null) {
            logger.warn("Order not found for ID: {}", id);

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")  // User will see this message
                    .build();
        }

        // Check for user existence before updating
        if (updatedOrder.getUser() != null && updatedOrder.getUser().id != null) {
            User user = User.findById(updatedOrder.getUser().id);
            if (user == null) {
                logger.warn("User not found for ID: {}", updatedOrder.getUser().id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found")   // User will see this message
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
        return Response.ok(message).build();
    }

    // Update an existing GUEST order by guestTrackingId
    @PUT
    @Path("{guestTrackingId}")
    @Transactional
    public Response updateGuestOrder(@PathParam("guestTrackingId") String guestTrackingId, Order updatedGuestOrder) {
        logger.info("Updating guest order with tracking ID: {}", guestTrackingId);

        // Find the existing guest order by guestTrackingId
        Order existingGuestOrder = Order.find("guestTrackingId", guestTrackingId).firstResult();

        if (existingGuestOrder == null) {
            logger.warn("Order not found for guestTrackingId: {}", guestTrackingId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")  // User will see this message
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
        String message = "Guest order updated successfully for guestTrackingId: " + existingGuestOrder.getGuestTrackingId();
        return Response.ok(existingGuestOrder).build();
    }

    // Delete an order by ID
    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteOrder(@PathParam("id") Long id) {
        logger.info("Deleting order with ID: {}", id);

        Order order = Order.findById(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")  // User will see this message
                    .build();
        }
        order.delete();
        logger.info("Order deleted successfully for ID: {}", id);
        // User will see this message
        String message = "Order deleted successfully for ID: " + id;
        return Response.ok(message).build(); // Return 204 No Content
    }

    // Delete an order by guestTrackingId
    @DELETE
    @Path("{guestTrackingId}")
    @Transactional
    public Response deleteGuestOrder(@PathParam("guestTrackingId") String guestTrackingId) {
        logger.info("Deleting guest order with tracking ID: {}", guestTrackingId);

        Order order =  Order.find("guestTrackingId", guestTrackingId).firstResult();
        if (order == null) {
            logger.warn("Order not found for guestTrackingId: {}", guestTrackingId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")  // User will see this message
                    .build();
        }
        order.delete();

        logger.info("Guest order deleted successfully for guestTrackingID: {}", guestTrackingId);
        // User will see this message
        String message = "Guest order deleted successfully for guestTrackingID: " + guestTrackingId;
        return Response.noContent().build(); // Return 204 No Content
    }
}