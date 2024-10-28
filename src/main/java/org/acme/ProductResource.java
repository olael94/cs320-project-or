package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    //The logger object is used to log messages to the console.
    private static final Logger logger = LoggerFactory.getLogger(ProductResource.class);

    // Create a new product
    @POST
    @Transactional
    public Response createProduct(Product product) {
        logger.info("Creating product: {}", product.getProductName());
        product.persist(); // Persist the product

        // User will see this message
        String message = "Product " + product.getProductName() + " created successfully";
        return Response.status(Response.Status.CREATED).entity(message).build();
    }

    // Get all products in the database.
    @GET
    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        return Product.listAll(); // Retrieve all products
    }

    // Get a product by ID
    @GET
    @Path("{id}")
    public Response getProduct(@PathParam("id") Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            logger.error("Product with ID {} not found", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found") // User will see this message
                    .build();
        }
        logger.info("Fetching product with ID {}", id);
        // User will see this message
        String message = "Product " + product.getProductName() + "with ID: " + product.id + " retrieved successfully";
        return Response.ok(message).build();
    }

    // Update a product by ID
    @PUT
    @Path("{id}")
    @Transactional
    public Response updateProduct(@PathParam("id") Long id, Product product) {
        Product existingProduct = Product.findById(id);
        if (existingProduct == null) {
            logger.error("Product with ID {} not found for update", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found") // User will see this message
                    .build();
        }
        existingProduct.setProductName(product.getProductName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setImageURL(product.getImageURL());
        existingProduct.persist();

        logger.info("Updated product with ID {}", id);
        // User will see this message
        String message = "Product " + existingProduct.getProductName() + " with ID: " + existingProduct.id + " updated successfully";
        return Response.ok(message).build();
    }

    // Delete a product by ID
    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteProduct(@PathParam("id") Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            logger.error("Product with ID {} not found for deletion", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found") // User will see this message
                    .build();
        }
        product.delete();
        logger.info("Deleted product with ID {}", id);
        // User will see this message
        String message = "Product " + product.getProductName() + " with ID: " + product.id + " deleted successfully";
        return Response.ok(message).build();
    }
}
