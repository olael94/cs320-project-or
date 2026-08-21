package org.acme;

import static io.restassured.RestAssured.given;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.UUID;
import org.acme.entity.PasswordResetToken;
import org.acme.entity.Session;
import org.acme.entity.User;

/** Shared helpers for auth-related tests: each test gets its own isolated account. */
public class TestAuthHelper {

  public static final String PASSWORD = "testpassword123";

  public record AuthenticatedUser(
      String email, String password, String sessionCookie, String csrfToken) {}

  public static String uniqueEmail() {
    return "test-" + UUID.randomUUID() + "@example.com";
  }

  public static Response register(String email, String password) {
    return given()
        .contentType("application/json")
        .body(
            "{\"username\":\"testuser\",\"email\":\""
                + email
                + "\",\"password\":\""
                + password
                + "\"}")
        .post("/api/users/register");
  }

  public static Response login(String email, String password) {
    return given()
        .contentType("application/json")
        .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
        .post("/api/users/login");
  }

  /** Registers a brand new user with a unique email and logs them in. */
  public static AuthenticatedUser registerAndLogin() {
    String email = uniqueEmail();
    register(email, PASSWORD).then().statusCode(201);

    Response loginResponse = login(email, PASSWORD);
    loginResponse.then().statusCode(200);

    String sessionCookie = loginResponse.getCookie("session");
    String csrfToken = loginResponse.getCookie("csrf_token");

    return new AuthenticatedUser(email, PASSWORD, sessionCookie, csrfToken);
  }

  /**
   * Reads the most recently issued (unused) password-reset token for an email directly from the
   * database. Tests can't read it out of an email - the mailer is mocked in the test profile - so
   * this is the only way to get a real token to exercise /reset-password/confirm.
   */
  public static String getLatestResetTokenFor(String email) {
    return QuarkusTransaction.requiringNew()
        .call(
            () -> {
              User user = User.find("email", email).firstResult();
              PasswordResetToken token =
                  PasswordResetToken.find("user = ?1 and used = false order by id desc", user)
                      .firstResult();
              return token.token;
            });
  }

  /** Directly overrides a session's expiry fields, so sliding-expiry tests don't have to wait. */
  public static void setSessionExpiry(
      String sessionToken, Instant expiresAt, Instant absoluteExpiresAt) {
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              Session session = Session.find("token", sessionToken).firstResult();
              session.expiresAt = expiresAt;
              session.absoluteExpiresAt = absoluteExpiresAt;
              session.persist();
            });
  }

  /**
   * Directly sets a user's role, bypassing the app - there's no self-service way to become an
   * admin.
   */
  public static void setUserRole(String email, User.Role role) {
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              User user = User.find("email", email).firstResult();
              user.setRole(role);
              user.persist();
            });
  }

  /** Directly deactivates a user, bypassing the app - there's no admin endpoint for this yet. */
  public static void setUserActive(String email, boolean active) {
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              User user = User.find("email", email).firstResult();
              user.setActive(active);
              user.persist();
            });
  }

  public static User.Role getUserRole(String email) {
    return QuarkusTransaction.requiringNew()
        .call(() -> User.find("email", email).<User>firstResult().getRole());
  }

  public static Instant getSessionExpiresAt(String sessionToken) {
    return QuarkusTransaction.requiringNew()
        .call(() -> Session.find("token", sessionToken).<Session>firstResult().expiresAt);
  }

  /**
   * Calls Session.findValid() with no ambient transaction, matching how it's actually called in
   * production (from filters, which aren't @Transactional).
   */
  public static Session findValidSession(String sessionToken) {
    return Session.findValid(sessionToken);
  }
}
