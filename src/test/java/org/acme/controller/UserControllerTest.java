package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.acme.TestAuthHelper.AuthenticatedUser;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.acme.TestAuthHelper;
import org.acme.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Covers every endpoint in UserController, grouped by concern. */
@QuarkusTest
class UserControllerTest {

  @Nested
  class Register {

    @Test
    void register_success_returnsUserDtoWithoutPassword() {
      String email = TestAuthHelper.uniqueEmail();

      TestAuthHelper.register(email, TestAuthHelper.PASSWORD)
          .then()
          .statusCode(201)
          .body("email", equalTo(email))
          .body("username", equalTo("testuser"))
          .body("password", equalTo(null)); // UserDto never carries the password hash
    }

    @Test
    void register_missingFields_returns400() {
      given()
          .contentType("application/json")
          .body("{\"email\":\"missingstuff@example.com\"}")
          .post("/api/users/register")
          .then()
          .statusCode(400);
    }

    @Test
    void register_duplicateEmail_returns409() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(409);
    }

    @Test
    void register_ignoresClientSuppliedRole_defaultsToCustomer() {
      String email = TestAuthHelper.uniqueEmail();

      given()
          .contentType("application/json")
          .body(
              "{\"username\":\"testuser\",\"email\":\""
                  + email
                  + "\",\"password\":\""
                  + TestAuthHelper.PASSWORD
                  + "\",\"role\":\"ADMIN\"}")
          .post("/api/users/register")
          .then()
          .statusCode(201);

      org.junit.jupiter.api.Assertions.assertEquals(
          java.util.Set.of(User.Role.CUSTOMER), TestAuthHelper.getUserRoles(email));
    }
  }

  @Nested
  class Login {

    @Test
    void login_success_returnsUserDtoAndCookies() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      Response response = TestAuthHelper.login(email, TestAuthHelper.PASSWORD);

      response.then().statusCode(200).body("email", equalTo(email));
      org.junit.jupiter.api.Assertions.assertNotNull(response.getCookie("session"));
      org.junit.jupiter.api.Assertions.assertNotNull(response.getCookie("csrf_token"));
    }

    @Test
    void login_wrongPassword_returns401WithGenericMessage() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      TestAuthHelper.login(email, "wrongpassword")
          .then()
          .statusCode(401)
          .body("message", equalTo("Invalid email or password"));
    }

    @Test
    void login_nonexistentEmail_returnsSameGenericMessageAsWrongPassword() {
      // Same message as a wrong password, on purpose - the endpoint shouldn't
      // reveal whether an email is registered at all.
      TestAuthHelper.login("nobody-" + TestAuthHelper.uniqueEmail(), "whatever")
          .then()
          .statusCode(401)
          .body("message", equalTo("Invalid email or password"));
    }

    @Test
    void login_missingFields_returns400() {
      given()
          .contentType("application/json")
          .body("{\"email\":\"onlyemail@example.com\"}")
          .post("/api/users/login")
          .then()
          .statusCode(400);
    }

    @Test
    void login_lockedAfterFiveFailedAttempts_rejectsEvenCorrectPassword() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      for (int i = 0; i < 5; i++) {
        TestAuthHelper.login(email, "wrongpassword").then().statusCode(401);
      }

      // The 6th attempt, even with the correct password, should be locked out.
      TestAuthHelper.login(email, TestAuthHelper.PASSWORD)
          .then()
          .statusCode(429)
          .body("message", notNullValue());
    }

    @Test
    void login_deactivatedAccount_returns401WithGenericMessage() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);
      TestAuthHelper.setUserActive(email, false);

      TestAuthHelper.login(email, TestAuthHelper.PASSWORD)
          .then()
          .statusCode(401)
          .body("message", equalTo("Invalid email or password"));
    }

    @Test
    void login_deactivatedAccount_doesNotCountAsFailedAttempt() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);
      TestAuthHelper.setUserActive(email, false);

      // Correct password, but deactivated - this must not be treated as a failed
      // attempt, or a deactivated user could get locked out for no reason.
      TestAuthHelper.login(email, TestAuthHelper.PASSWORD).then().statusCode(401);

      TestAuthHelper.setUserActive(email, true);
      TestAuthHelper.login(email, TestAuthHelper.PASSWORD).then().statusCode(200);
    }
  }

  @Nested
  class LogoutAndMe {

    @Test
    void logout_invalidatesTheSession() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given().cookie("session", user.sessionCookie()).get("/api/users/me").then().statusCode(200);

      given()
          .cookie("session", user.sessionCookie())
          .header("X-CSRF-Token", user.csrfToken())
          .post("/api/users/logout")
          .then()
          .statusCode(200);

      given().cookie("session", user.sessionCookie()).get("/api/users/me").then().statusCode(401);
    }

    @Test
    void me_noSession_returns401() {
      given().get("/api/users/me").then().statusCode(401);
    }

    @Test
    void me_validSession_returnsCurrentUser() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .get("/api/users/me")
          .then()
          .statusCode(200)
          .body("email", equalTo(user.email()))
          .body("roles", contains("CUSTOMER"))
          .body("active", equalTo(true));
    }

    @Test
    void me_garbageSessionToken_returns401() {
      given()
          .cookie("session", "this-is-not-a-real-token")
          .get("/api/users/me")
          .then()
          .statusCode(401);
    }
  }

  @Nested
  class Profile {

    @Test
    void getAllUsers_noSession_returns401() {
      given().get("/api/users").then().statusCode(401);
    }

    @Test
    void getAllUsers_asAdmin_returns200() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);

      given().cookie("session", admin.sessionCookie()).get("/api/users").then().statusCode(200);
    }

    @Test
    void getAllUsers_nonAdmin_returns403() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given().cookie("session", user.sessionCookie()).get("/api/users").then().statusCode(403);
    }

    @Test
    void getUser_ownId_returns200() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();
      long id =
          given()
              .cookie("session", user.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", user.sessionCookie())
          .get("/api/users/" + id)
          .then()
          .statusCode(200)
          .body("email", equalTo(user.email()));
    }

    @Test
    void getUser_someoneElsesId_returns403() {
      AuthenticatedUser userA = TestAuthHelper.registerAndLogin();
      AuthenticatedUser userB = TestAuthHelper.registerAndLogin();
      long idB =
          given()
              .cookie("session", userB.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", userA.sessionCookie())
          .get("/api/users/" + idB)
          .then()
          .statusCode(403);
    }

    @Test
    void getUser_noSession_returns401() {
      given().get("/api/users/1").then().statusCode(401);
    }

    @Test
    void updateCurrentUser_success_changesUsername() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .header("X-CSRF-Token", user.csrfToken())
          .contentType("application/json")
          .body("{\"username\":\"updatedname\"}")
          .put("/api/users/me")
          .then()
          .statusCode(200)
          .body("username", equalTo("updatedname"))
          .body("email", equalTo(user.email())); // unchanged
    }

    @Test
    void updateCurrentUser_noSession_returns401() {
      given()
          .contentType("application/json")
          .body("{\"username\":\"nope\"}")
          .put("/api/users/me")
          .then()
          .statusCode(401);
    }

    @Test
    void changePassword_wrongCurrentPassword_returns401() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .header("X-CSRF-Token", user.csrfToken())
          .contentType("application/json")
          .body("{\"currentPassword\":\"wrongpassword\",\"newPassword\":\"newpass456\"}")
          .post("/api/users/me/change-password")
          .then()
          .statusCode(401);
    }

    @Test
    void changePassword_success_oldPasswordStopsWorkingNewPasswordWorks() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .header("X-CSRF-Token", user.csrfToken())
          .contentType("application/json")
          .body(
              "{\"currentPassword\":\""
                  + user.password()
                  + "\",\"newPassword\":\"brandnewpass789\"}")
          .post("/api/users/me/change-password")
          .then()
          .statusCode(200);

      TestAuthHelper.login(user.email(), user.password()).then().statusCode(401);
      TestAuthHelper.login(user.email(), "brandnewpass789").then().statusCode(200);
    }

    @Test
    void changePassword_killsOtherSessionsButKeepsTheCurrentOne() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      Response loginA = TestAuthHelper.login(email, TestAuthHelper.PASSWORD);
      String sessionA = loginA.getCookie("session");
      String csrfA = loginA.getCookie("csrf_token");

      Response loginB = TestAuthHelper.login(email, TestAuthHelper.PASSWORD);
      String sessionB = loginB.getCookie("session");

      given()
          .cookie("session", sessionA)
          .header("X-CSRF-Token", csrfA)
          .contentType("application/json")
          .body(
              "{\"currentPassword\":\""
                  + TestAuthHelper.PASSWORD
                  + "\",\"newPassword\":\"anotherpass000\"}")
          .post("/api/users/me/change-password")
          .then()
          .statusCode(200);

      // Session A made the change and survives.
      given().cookie("session", sessionA).get("/api/users/me").then().statusCode(200);
      // Session B is a different, concurrent login and gets killed.
      given().cookie("session", sessionB).get("/api/users/me").then().statusCode(401);
    }

    @Test
    void deleteCurrentUser_success_accountIsGone() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .header("X-CSRF-Token", user.csrfToken())
          .delete("/api/users/me")
          .then()
          .statusCode(200);

      TestAuthHelper.login(user.email(), user.password()).then().statusCode(401);
    }

    @Test
    void deleteCurrentUser_noSession_returns401() {
      given().delete("/api/users/me").then().statusCode(401);
    }
  }

  @Nested
  class DeactivateReactivate {

    @Test
    void deactivateUser_asAdmin_success_killsTargetSessionAndBlocksLogin() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);

      AuthenticatedUser target = TestAuthHelper.registerAndLogin();
      long targetId =
          given()
              .cookie("session", target.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + targetId + "/deactivate")
          .then()
          .statusCode(200);

      // The target's already-existing session must be killed immediately, not
      // just future logins blocked.
      given().cookie("session", target.sessionCookie()).get("/api/users/me").then().statusCode(401);

      TestAuthHelper.login(target.email(), target.password())
          .then()
          .statusCode(401)
          .body("message", equalTo("Invalid email or password"));
    }

    @Test
    void deactivateUser_nonAdmin_returns403() {
      AuthenticatedUser nonAdmin = TestAuthHelper.registerAndLogin();
      AuthenticatedUser target = TestAuthHelper.registerAndLogin();
      long targetId =
          given()
              .cookie("session", target.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", nonAdmin.sessionCookie())
          .header("X-CSRF-Token", nonAdmin.csrfToken())
          .post("/api/users/" + targetId + "/deactivate")
          .then()
          .statusCode(403);
    }

    @Test
    void deactivateUser_noSession_returns401() {
      given().post("/api/users/1/deactivate").then().statusCode(401);
    }

    @Test
    void deactivateUser_targetNotFound_returns404() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);
      long adminId =
          given()
              .cookie("session", admin.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");
      long nonexistentId = adminId + 1_000_000L;

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + nonexistentId + "/deactivate")
          .then()
          .statusCode(404);
    }

    @Test
    void deactivateUser_ownAccount_returns400() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);
      long adminId =
          given()
              .cookie("session", admin.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + adminId + "/deactivate")
          .then()
          .statusCode(400)
          .body("message", equalTo("You cannot deactivate your own account"));
    }

    @Test
    void reactivateUser_asAdmin_success_targetCanLoginAgain() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);

      AuthenticatedUser target = TestAuthHelper.registerAndLogin();
      long targetId =
          given()
              .cookie("session", target.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");
      TestAuthHelper.setUserActive(target.email(), false);

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + targetId + "/reactivate")
          .then()
          .statusCode(200);

      TestAuthHelper.login(target.email(), target.password()).then().statusCode(200);
    }

    @Test
    void reactivateUser_nonAdmin_returns403() {
      AuthenticatedUser nonAdmin = TestAuthHelper.registerAndLogin();
      AuthenticatedUser target = TestAuthHelper.registerAndLogin();
      long targetId =
          given()
              .cookie("session", target.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", nonAdmin.sessionCookie())
          .header("X-CSRF-Token", nonAdmin.csrfToken())
          .post("/api/users/" + targetId + "/reactivate")
          .then()
          .statusCode(403);
    }

    @Test
    void reactivateUser_targetNotFound_returns404() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);
      long adminId =
          given()
              .cookie("session", admin.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");
      long nonexistentId = adminId + 1_000_000L;

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + nonexistentId + "/reactivate")
          .then()
          .statusCode(404);
    }
  }

  @Nested
  class AdminPasswordReset {

    @Test
    void adminRequestPasswordReset_asAdmin_success_targetCanCompleteReset() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);

      AuthenticatedUser target = TestAuthHelper.registerAndLogin();
      long targetId =
          given()
              .cookie("session", target.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + targetId + "/reset-password")
          .then()
          .statusCode(200);

      // The triggered reset must produce a real, usable token, not just a
      // 200 response - complete the flow end to end.
      String token = TestAuthHelper.getLatestResetTokenFor(target.email());
      given()
          .contentType("application/json")
          .body("{\"token\":\"" + token + "\",\"newPassword\":\"newpassword456\"}")
          .post("/api/users/reset-password/confirm")
          .then()
          .statusCode(200);

      TestAuthHelper.login(target.email(), "newpassword456").then().statusCode(200);
    }

    @Test
    void adminRequestPasswordReset_nonAdmin_returns403() {
      AuthenticatedUser nonAdmin = TestAuthHelper.registerAndLogin();
      AuthenticatedUser target = TestAuthHelper.registerAndLogin();
      long targetId =
          given()
              .cookie("session", target.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");

      given()
          .cookie("session", nonAdmin.sessionCookie())
          .header("X-CSRF-Token", nonAdmin.csrfToken())
          .post("/api/users/" + targetId + "/reset-password")
          .then()
          .statusCode(403);
    }

    @Test
    void adminRequestPasswordReset_noSession_returns401() {
      given().post("/api/users/1/reset-password").then().statusCode(401);
    }

    @Test
    void adminRequestPasswordReset_targetNotFound_returns404() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.setUserRole(admin.email(), User.Role.ADMIN);
      long adminId =
          given()
              .cookie("session", admin.sessionCookie())
              .get("/api/users/me")
              .jsonPath()
              .getLong("id");
      long nonexistentId = adminId + 1_000_000L;

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .post("/api/users/" + nonexistentId + "/reset-password")
          .then()
          .statusCode(404);
    }
  }

  @Nested
  class PasswordReset {

    private static final String GENERIC_MESSAGE =
        "If that email is registered, a reset link has been sent.";

    private void requestReset(String email) {
      given()
          .contentType("application/json")
          .body("{\"email\":\"" + email + "\"}")
          .post("/api/users/reset-password/request")
          .then()
          .statusCode(200)
          .body("message", equalTo(GENERIC_MESSAGE));
    }

    private void confirmReset(String token, String newPassword, int expectedStatus) {
      given()
          .contentType("application/json")
          .body("{\"token\":\"" + token + "\",\"newPassword\":\"" + newPassword + "\"}")
          .post("/api/users/reset-password/confirm")
          .then()
          .statusCode(expectedStatus);
    }

    @Test
    void requestReset_existingEmail_returnsGenericMessage() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      requestReset(email);
    }

    @Test
    void requestReset_nonexistentEmail_returnsSameGenericMessage() {
      // Same response either way, so this endpoint can't be used to check which
      // emails are registered.
      requestReset("nobody-" + TestAuthHelper.uniqueEmail());
    }

    @Test
    void validateToken_missing_returns400() {
      given().get("/api/users/reset-password/validate").then().statusCode(400);
    }

    @Test
    void validateToken_invalid_returns401() {
      given()
          .queryParam("token", "not-a-real-token")
          .get("/api/users/reset-password/validate")
          .then()
          .statusCode(401);
    }

    @Test
    void validateToken_valid_returns200() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);
      requestReset(email);
      String token = TestAuthHelper.getLatestResetTokenFor(email);

      given()
          .queryParam("token", token)
          .get("/api/users/reset-password/validate")
          .then()
          .statusCode(200);
    }

    @Test
    void confirmReset_invalidToken_returns401() {
      confirmReset("not-a-real-token", "newpassword000", 401);
    }

    @Test
    void confirmReset_success_oldPasswordStopsWorkingNewPasswordWorks() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);
      requestReset(email);
      String token = TestAuthHelper.getLatestResetTokenFor(email);

      confirmReset(token, "resetpassword111", 200);

      TestAuthHelper.login(email, TestAuthHelper.PASSWORD).then().statusCode(401);
      TestAuthHelper.login(email, "resetpassword111").then().statusCode(200);
    }

    @Test
    void confirmReset_tokenIsSingleUse() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);
      requestReset(email);
      String token = TestAuthHelper.getLatestResetTokenFor(email);

      confirmReset(token, "firstresetpass222", 200);
      // Reusing the same token a second time must fail, even though it hasn't expired.
      confirmReset(token, "secondresetpass333", 401);
    }

    @Test
    void confirmReset_killsExistingSessions() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      requestReset(user.email());
      String token = TestAuthHelper.getLatestResetTokenFor(user.email());
      confirmReset(token, "resetpass444", 200);

      given().cookie("session", user.sessionCookie()).get("/api/users/me").then().statusCode(401);
    }

    @Test
    void requestReset_invalidatesThePreviousUnusedToken() {
      String email = TestAuthHelper.uniqueEmail();
      TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);

      requestReset(email);
      String firstToken = TestAuthHelper.getLatestResetTokenFor(email);

      requestReset(email);
      String secondToken = TestAuthHelper.getLatestResetTokenFor(email);

      // The first (older) link should no longer work once a second was requested.
      confirmReset(firstToken, "shouldnotwork555", 401);
      confirmReset(secondToken, "shouldwork666", 200);
    }
  }
}
