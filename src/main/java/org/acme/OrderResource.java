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

        if (order.user != null) {
            // Fetch and validate the user if provided
            User user = User.findById(order.user.id);
            if (user == null) {
                logger.warn("User not found for ID: {}", order.user.id);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("User not found")
                        .build();
            }
            order.user = user;
        }

        order.orderDate = LocalDateTime.now();

        // The guestTrackingId will be generated automatically by the @PrePersist method

        order.persist(); // Persist the order to the database

        // Return the tracking ID for guest orders or the order ID for users
        String trackingInfo = order.user == null
                ? "Your order tracking number is " + order.guestTrackingId
                : "Your order reference number is " + order.id;

        logger.info("Order created successfully with tracking info: {}", trackingInfo);

        return Response.status(Response.Status.CREATED)
                .entity(trackingInfo)
                .build();
    }

    // Get all orders
    @GET
    public List<Order> getAllOrders() {
        return Order.listAll();
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
                    .entity("Order not found")
                    .build();
        }
        return Response.ok(order).build();
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
                    .entity("Order not found")
                    .build();
        }
        return Response.ok(order).build();
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
                    .entity("Order not found")
                    .build();
        }

        // Check for user existence before updating
        if (updatedOrder.user != null && updatedOrder.user.id != null) {
            User user = User.findById(updatedOrder.user.id);
            if (user == null) {
                logger.warn("User not found for ID: {}", updatedOrder.user.id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found")
                        .build();
            }
            existingOrder.user = user;
        }

        // Update order fields
        existingOrder.orderDate = updatedOrder.orderDate;
        existingOrder.totalAmount = updatedOrder.totalAmount;
        existingOrder.status = updatedOrder.status;
        existingOrder.persist();

        logger.info("Order updated successfully for ID: {}", id);

        return Response.ok(existingOrder).build();
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
                    .entity("Order not found")
                    .build();
        }

        // Update order fields
        existingGuestOrder.orderDate = updatedGuestOrder.orderDate;
        existingGuestOrder.totalAmount = updatedGuestOrder.totalAmount;
        existingGuestOrder.status = updatedGuestOrder.status;

        // Persist or update the order
        existingGuestOrder.persist();

        logger.info("Guest order updated successfully for guestTrackingId: {}", guestTrackingId);

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
                    .entity("Order not found")
                    .build();
        }
        order.delete();
        logger.info("Order deleted successfully for ID: {}", id);
        return Response.noContent().build(); // Return 204 No Content
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
                    .entity("Order not found")
                    .build();
        }
        order.delete();

        logger.info("Guest order deleted successfully for tracking ID: {}", guestTrackingId);
        return Response.noContent().build(); // Return 204 No Content
    }
}