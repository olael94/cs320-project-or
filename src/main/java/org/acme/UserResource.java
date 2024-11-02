package org.acme;

import jakarta.transaction.Transactional;
import org.acme.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;
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
    @Path("/register")
    @Transactional
    public Response createUser(User user) {
        logger.info("Creating user: {}", user.getUsername()); // Log the username of the user being created.

        // Check if username, email, or password is empty
        if ((user.getUsername() == null || user.getUsername().isEmpty()) ||
                (user.getEmail() == null || user.getEmail().isEmpty()) ||
                (user.getPassword() == null || user.getPassword().isEmpty())) {
            // If any of the fields are empty, return a bad request response (HTTP 400)
            return Response.status(Response.Status.BAD_REQUEST).entity("Username, email, and password are required").build();
        }

        // Check if a user with the same email already exists
        User existingUser = User.find("email", user.getEmail()).firstResult();
        if (existingUser != null) {
            // If a user with the same email exists, return a conflict response (HTTP 409)
            return Response.status(Response.Status.CONFLICT).entity("Email is already in use").build();
        }

        user.persist(); // Persist the user to the database.
        String message = "Welcome " + user.getUsername() + "! you have successfully created your Store account using " + user.getEmail();
        return Response.status(Response.Status.CREATED).entity(message).build(); // Return the created user with a status code of 201 (CREATED).
    }

    // Login a user
    @POST
    @Path("/login")
    @Transactional
    public Response loginUser(LoginDto loginDto) {
        logger.info("Logging in user: {}", loginDto.getEmail());

        // Check if email or password is empty
        if ((loginDto.getEmail() == null || loginDto.getEmail().isEmpty()) ||
                (loginDto.getPassword() == null || loginDto.getPassword().isEmpty())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and password are required").build();
        }

        // Find the user by email
        User user = User.find("email", loginDto.getEmail()).firstResult();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email").build();
        }

        // Check if the provided password matches the stored password
        if (!user.checkPassword(loginDto.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email password").build();
        }

        // If the email and password are correct, return an OK response (HTTP 200) and a welcome message
        String message = "Welcome back! " + user.getUsername();
        return Response.ok(message).build();
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
        String message = "Your Username with ID " + user.id + "was found: " + user.getUsername();
        return Response.ok(message).build();
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
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setRole(user.getRole());
        existingUser.persist();

        logger.info("Updated user with ID {}", id);
        String message = "Your Account info. with ID " + user.id + "was updated.";
        return Response.ok(message).build();
    }

    // Reset a user's password with an email
    @POST
    @Path("/reset-password")
    @Transactional
    public Response resetPassword(PasswordResetDto passwordResetDto) {
        logger.info("Resetting password for email: {}", passwordResetDto.getEmail());

        // Check if email or new password is empty
        if (passwordResetDto.getEmail() == null || passwordResetDto.getEmail().isEmpty() ||
                passwordResetDto.getNewPassword() == null || passwordResetDto.getNewPassword().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and new password are required").build();
        }

        // Find the user by email
        User user = User.find("email", passwordResetDto.getEmail()).firstResult();
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Email not found").build();
        }

        // Log the old password for debugging (do not do this in production)
        logger.info("Old hashed password: {}", user.getPassword());

        // Update the user's password without hashing it again
        user.setPassword(passwordResetDto.getNewPassword());
        user.persist();
        user.getEntityManager().flush(); // Force the persistence context to synchronize with the database

        // Verify the password was updated
        User updatedUser = User.find("email", passwordResetDto.getEmail()).firstResult();
        logger.info("Updated hashed password in DB: {}", updatedUser.getPassword());

        // Log the new hashed password for debugging (do not do this in production)
        logger.info("New hashed password: {}", user.getPassword());

        String message = "Password reset successfully.";
        return Response.ok(message).build();
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
        String message = "Your Account with ID " + existingUser.id + "was deleted.";
        return Response.ok(message).build();
    }
}
