package org.acme.controller;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.acme.dto.LoginDto;
import org.acme.dto.PasswordResetDto;
import org.acme.dto.UserDto;
import org.acme.entity.Session;
import org.acme.entity.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
  // The logger object is used to log messages to the console.
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  // Generates a cryptographically random token for session and CSRF identifiers.
  private static final SecureRandom RANDOM = new SecureRandom();

  private static String generateToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  // Inject the configuration properties using CDI to
  @Inject
  @ConfigProperty(name = "app.cookie.secure")
  boolean cookieSecure;

  // Create a new User
  @POST
  @Path("/register")
  @Transactional
  public Response createUser(User user) {
    logger.info("Creating user: {}", user.getUsername());

    // Check if username, email, or password is empty
    if ((user.getUsername() == null || user.getUsername().isEmpty())
        || (user.getEmail() == null || user.getEmail().isEmpty())
        || (user.getPassword() == null || user.getPassword().isEmpty())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Username, email, and password are required")
          .build();
    }

    // Check if a user with the same email already exists
    User existingUser = User.find("email", user.getEmail()).firstResult();
    if (existingUser != null) {
      return Response.status(Response.Status.CONFLICT).entity("Email is already in use").build();
    }

    // The password is already hashed at this point: Jackson called setPassword() while
    // deserializing the request body, which hashes it in User.setPassword().
    user.persist();

    String message =
        "Welcome "
            + user.getUsername()
            + "! You have successfully created your Store account using "
            + user.getEmail();
    return Response.status(Response.Status.CREATED).entity(message).build();
  }

  // Login a user
  @POST
  @Path("/login")
  @Transactional
  public Response loginUser(LoginDto loginDto) {
    logger.info("Logging in user: {}", loginDto.getEmail());

    // Check if email or password is empty
    if ((loginDto.getEmail() == null || loginDto.getEmail().isEmpty())
        || (loginDto.getPassword() == null || loginDto.getPassword().isEmpty())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Email and password are required")
          .build();
    }

    // Find the user by email
    User user = User.find("email", loginDto.getEmail()).firstResult();
    if (user == null || !user.checkPassword(loginDto.getPassword())) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Invalid email or password")
          .build();
    }

    logger.info("User logged in successfully: {}", user.getUsername());

    // Create a new session for the user
    Session session = new Session();
    session.user = user;
    session.token = generateToken();
    session.csrfToken = generateToken();
    session.expiresAt = Instant.now().plus(7, ChronoUnit.DAYS); // 7 days from now to expire
    session.persist();

    logger.info("Session created for user: {}", user.getUsername());

    // Create a new cookie for the session token. This means the cookie will be sent with every
    // request to the server, allowing the server to identify the user and maintain their session.
    NewCookie sessionCookie =
        new NewCookie.Builder("session")
            .value(session.token)
            .path("/")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(7 * 24 * 60 * 60)
            .build();

    NewCookie csrfCookie =
        new NewCookie.Builder("csrf_token")
            .value(session.csrfToken)
            .path("/")
            .httpOnly(false)
            .secure(cookieSecure)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(7 * 24 * 60 * 60)
            .build();

    // If the login is successful, a 200 (OK) status code is returned along with the user data and
    // the session and CSRF cookies.
    return Response.ok(new UserDto(user)).cookie(sessionCookie, csrfCookie).build();
  }

  @POST
  @Path("/logout")
  @Transactional
  public Response logoutUser(@CookieParam("session") Cookie sessionCookie) {
    // If the user is logged in, delete the session token from the database.
    if (sessionCookie != null) {
      Session.delete("token", sessionCookie.getValue());
    }

    NewCookie expiredSessionCookie =
        new NewCookie.Builder("session")
            .value("")
            .path("/")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(0) // Expire the cookie immediately
            .build();

    NewCookie expiredCsrf =
        new NewCookie.Builder("csrf_token")
            .value("")
            .path("/")
            .httpOnly(false)
            .secure(cookieSecure)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(0) // Expire the cookie immediately
            .build();
    // If the logout is successful, a 200 (OK) status code is returned along with the expired
    // session and CSRF cookies.
    return Response.ok().cookie(expiredSessionCookie, expiredCsrf).build();
  }

  @GET
  @Path("/me")
  public Response me(@CookieParam("session") Cookie sessionCookie) {
    // Check if the session cookie is present
    if (sessionCookie == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    // Find the session by token
    Session session = Session.findValid(sessionCookie.getValue());
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    return Response.ok(new UserDto(session.user)).build();
  }

  // Get all users in the database.
  @GET
  public List<UserDto> getAllUsers() {
    logger.info("Fetching all users");
    List<User> users = User.listAll();
    return users.stream().map(UserDto::new).collect(Collectors.toList());
  }

  // Get a user by ID
  @GET
  @Path("{id}")
  public Response getUser(@PathParam("id") Long id) {

    User user = User.findById(id);
    // If the user is not found, a 404 (NOT FOUND) status code is returned.
    if (user == null) {
      logger.error("User with ID {} not found", id);
      return Response.status(
              Response.Status
                  .NOT_FOUND) // The Response object is used to return a 404 status code with an
          // error message.
          .entity("User not found")
          .build(); // build() method is used to build the response object.
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
      return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
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
    if (passwordResetDto.getEmail() == null
        || passwordResetDto.getEmail().isEmpty()
        || passwordResetDto.getNewPassword() == null
        || passwordResetDto.getNewPassword().isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Email and new password are required")
          .build();
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
    user.getEntityManager()
        .flush(); // Force the persistence context to synchronize with the database

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
      return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
    }
    existingUser.delete();
    logger.info("Deleted user with ID {}", id);
    String message = "Your Account with ID " + existingUser.id + "was deleted.";
    return Response.ok(message).build();
  }
}
