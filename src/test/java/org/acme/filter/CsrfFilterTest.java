package org.acme.filter;

import static io.restassured.RestAssured.given;
import static org.acme.TestAuthHelper.AuthenticatedUser;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.TestAuthHelper;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CsrfFilterTest {

  @Test
  void stateChangingRequest_withSessionButNoCsrfHeader_returns403() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();

    given()
        .cookie("session", user.sessionCookie())
        .post("/api/users/logout")
        .then()
        .statusCode(403);
  }

  @Test
  void stateChangingRequest_withSessionAndWrongCsrfHeader_returns403() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();

    given()
        .cookie("session", user.sessionCookie())
        .header("X-CSRF-Token", "definitely-the-wrong-token")
        .post("/api/users/logout")
        .then()
        .statusCode(403);
  }

  @Test
  void stateChangingRequest_withSessionAndCorrectCsrfHeader_isAllowedThrough() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();

    given()
        .cookie("session", user.sessionCookie())
        .header("X-CSRF-Token", user.csrfToken())
        .post("/api/users/logout")
        .then()
        .statusCode(200);
  }

  @Test
  void stateChangingRequest_withNoSessionAtAll_isAllowedThroughTheFilter() {
    // No session cookie means there's nothing CSRF-relevant to protect - this
    // is exactly why register/login work without a CSRF header at all.
    String email = TestAuthHelper.uniqueEmail();

    TestAuthHelper.register(email, TestAuthHelper.PASSWORD).then().statusCode(201);
    TestAuthHelper.login(email, TestAuthHelper.PASSWORD).then().statusCode(200);
  }

  @Test
  void safeMethod_withSessionAndNoCsrfHeader_isAllowedThrough() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();

    // GET is a safe method, so the filter shouldn't require a CSRF header here.
    given().cookie("session", user.sessionCookie()).get("/api/users/me").then().statusCode(200);
  }
}
