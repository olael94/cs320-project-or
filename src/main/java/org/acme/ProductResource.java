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

    //The createProduct method takes a Product object as input and persists (adds) it to the database.
    @POST
    @Transactional
    public Response createProduct(Product product) {
        logger.info("Creating product: {}", product.productName);
        product.persist(); // Persist the product
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    //The getAllProducts method returns all products in the database.
    @GET
    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        return Product.listAll(); // Retrieve all products
    }

    //The getProduct method takes an ID as input and returns the product with that ID.
    @GET
    @Path("{id}")
    public Response getProduct(@PathParam("id") Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            logger.error("Product with ID {} not found", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found")
                    .build();
        }
        logger.info("Fetching product with ID {}", id);
        return Response.ok(product).build();
    }

    //The updateProduct method takes an ID and a Product object as input and updates the product with that ID.
    @PUT
    @Path("{id}")
    @Transactional
    public Response updateProduct(@PathParam("id") Long id, Product product) {
        Product existingProduct = Product.findById(id);
        if (existingProduct == null) {
            logger.error("Product with ID {} not found for update", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found")
                    .build();
        }
        existingProduct.productName = product.productName;
        existingProduct.description = product.description;
        existingProduct.price = product.price;
        existingProduct.imageURL = product.imageURL;
        existingProduct.persist();
        logger.info("Updated product with ID {}", id);
        return Response.ok(existingProduct).build();
    }

    //The deleteProduct method takes an ID as input and deletes the product with that ID.
    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteProduct(@PathParam("id") Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            logger.error("Product with ID {} not found for deletion", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found")
                    .build();
        }
        product.delete();
        logger.info("Deleted product with ID {}", id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
