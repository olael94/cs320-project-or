package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.acme.TestAuthHelper.AuthenticatedUser;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.TestAuthHelper;
import org.acme.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Covers every endpoint in OrderController, grouped by concern. */
@QuarkusTest
class OrderControllerTest {

  private long idOf(AuthenticatedUser user) {
    return given()
        .cookie("session", user.sessionCookie())
        .get("/api/users/me")
        .jsonPath()
        .getLong("id");
  }

  @Nested
  class GetOrder {

    @Test
    void getOrder_asOwner_returns200() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      given()
          .cookie("session", owner.sessionCookie())
          .get("/api/orders/" + orderId)
          .then()
          .statusCode(200);
    }

    @Test
    void getOrder_asAdmin_forSomeoneElseOrder_returns200() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given()
          .cookie("session", admin.sessionCookie())
          .get("/api/orders/" + orderId)
          .then()
          .statusCode(200);
    }

    @Test
    void getOrder_asNonOwnerNonAdmin_returns403() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      AuthenticatedUser someoneElse = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", someoneElse.sessionCookie())
          .get("/api/orders/" + orderId)
          .then()
          .statusCode(403);
    }

    @Test
    void getOrder_noSession_returns401() {
      given().get("/api/orders/1").then().statusCode(401);
    }

    @Test
    void getOrder_orderNotFound_returns404() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .get("/api/orders/999999999")
          .then()
          .statusCode(404);
    }

    @Test
    void getOrder_guestOrder_nonAdmin_returns403() {
      long orderId = TestAuthHelper.createGuestOrder(9.99);
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given()
          .cookie("session", user.sessionCookie())
          .get("/api/orders/" + orderId)
          .then()
          .statusCode(403);
    }

    @Test
    void getOrder_guestOrder_asAdmin_returns200() {
      long orderId = TestAuthHelper.createGuestOrder(9.99);
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given()
          .cookie("session", admin.sessionCookie())
          .get("/api/orders/" + orderId)
          .then()
          .statusCode(200);
    }
  }

  @Nested
  class UpdateOrder {

    private static final String VALID_UPDATE_BODY =
        "{\"orderDate\":\"2026-01-01T10:00:00\",\"totalAmount\":29.99,\"status\":\"COMPLETED\"}";

    @Test
    void updateOrder_asAdmin_returns200() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .contentType("application/json")
          .body(VALID_UPDATE_BODY)
          .put("/api/orders/" + orderId)
          .then()
          .statusCode(200);
    }

    @Test
    void updateOrder_nonAdminEvenIfOwner_returns403() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      given()
          .cookie("session", owner.sessionCookie())
          .header("X-CSRF-Token", owner.csrfToken())
          .contentType("application/json")
          .body(VALID_UPDATE_BODY)
          .put("/api/orders/" + orderId)
          .then()
          .statusCode(403);
    }

    @Test
    void updateOrder_noSession_returns401() {
      given()
          .contentType("application/json")
          .body(VALID_UPDATE_BODY)
          .put("/api/orders/1")
          .then()
          .statusCode(401);
    }

    @Test
    void updateOrder_orderNotFound_returns404() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .contentType("application/json")
          .body(VALID_UPDATE_BODY)
          .put("/api/orders/999999999")
          .then()
          .statusCode(404);
    }
  }

  @Nested
  class DeleteOrder {

    @Test
    void deleteOrder_asAdmin_returns200() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .delete("/api/orders/" + orderId)
          .then()
          .statusCode(200);
    }

    @Test
    void deleteOrder_nonAdminEvenIfOwner_returns403() {
      AuthenticatedUser owner = TestAuthHelper.registerAndLogin();
      long orderId = TestAuthHelper.createOrderForUser(idOf(owner), 19.99);

      given()
          .cookie("session", owner.sessionCookie())
          .header("X-CSRF-Token", owner.csrfToken())
          .delete("/api/orders/" + orderId)
          .then()
          .statusCode(403);
    }

    @Test
    void deleteOrder_noSession_returns401() {
      given().delete("/api/orders/1").then().statusCode(401);
    }

    @Test
    void deleteOrder_orderNotFound_returns404() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given()
          .cookie("session", admin.sessionCookie())
          .header("X-CSRF-Token", admin.csrfToken())
          .delete("/api/orders/999999999")
          .then()
          .statusCode(404);
    }
  }

  @Nested
  class GetAllOrders {

    @Test
    void getAllOrders_asAdmin_returns200() {
      AuthenticatedUser admin = TestAuthHelper.registerAndLogin();
      TestAuthHelper.addUserRole(admin.email(), User.Role.ADMIN);

      given().cookie("session", admin.sessionCookie()).get("/api/orders").then().statusCode(200);
    }

    @Test
    void getAllOrders_nonAdmin_returns403() {
      AuthenticatedUser user = TestAuthHelper.registerAndLogin();

      given().cookie("session", user.sessionCookie()).get("/api/orders").then().statusCode(403);
    }

    @Test
    void getAllOrders_noSession_returns401() {
      given().get("/api/orders").then().statusCode(401);
    }
  }
}
