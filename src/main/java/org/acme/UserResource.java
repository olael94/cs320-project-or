package org.acme;

import jakarta.transaction.Transactional;
import org.acme.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    //The logger object is used to log messages to the console.
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    // Create a new User
    @POST
    @Transactional
    public Response createUser(User user) {
        logger.info("Creating user: {}", user.username);                        //The logger.info method is used to log the username of the user being created.

        // Check if a user with the same email already exists
        User existingUser = User.find("email", user.email).firstResult();
        if (existingUser != null) {
            // If a user with the same email exists, return a conflict response (HTTP 409)
            return Response.status(Response.Status.CONFLICT).entity("Email is already in use").build();
        }

        user.persist();                                                         //The persist method is used to add the user to the database.
        return Response.status(Response.Status.CREATED).entity(user).build();   //The Response object is used to return the created user with a status code of 201 (CREATED).
    }

    // Get all users in the database.
    @GET
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return User.listAll();
    }

    // Get a user by ID
    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") Long id) {

        User user = User.findById(id);
        //If the user is not found, a 404 (NOT FOUND) status code is returned.
        if (user == null) {
            logger.error("User with ID {} not found", id);
            return Response.status(Response.Status.NOT_FOUND)                   //The Response object is used to return a 404 status code with an error message.
                    .entity("User not found")
                    .build();                                                   //build() method is used to build the response object.
        }
        logger.info("Fetching user with ID {}", id);
        return Response.ok(user).build();
    }

    // Update a user by ID
    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, User user) {
        User existingUser = User.findById(id);
        if (existingUser == null) {
            logger.error("User with ID {} not found for update", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }
        existingUser.username = user.username;
        existingUser.email = user.email;
        existingUser.password = user.password;
        existingUser.role = user.role;
        existingUser.persist();

        logger.info("Updated user with ID {}", id);
        return Response.ok(existingUser).build();
    }

    // Delete a user by ID
    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User existingUser = User.findById(id);
        if (existingUser == null) {
            logger.error("User with ID {} not found for deletion", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }
        existingUser.delete();
        logger.info("Deleted user with ID {}", id);
        return Response.noContent().build();
    }
}
