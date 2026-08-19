package org.acme.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.acme.TestAuthHelper;
import org.acme.TestAuthHelper.AuthenticatedUser;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SessionActivityFilterTest {

  @Test
  void authenticatedRequest_getsARefreshedSessionCookie() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();

    given()
        .cookie("session", user.sessionCookie())
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .cookie("session", notNullValue())
        .cookie("csrf_token", notNullValue());
  }

  @Test
  void logout_emitsExactlyOneSetCookiePerCookieName() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();

    Response response =
        given()
            .cookie("session", user.sessionCookie())
            .header("X-CSRF-Token", user.csrfToken())
            .post("/api/users/logout");

    response.then().statusCode(200);

    List<String> setCookieHeaders = response.getHeaders().getValues("Set-Cookie");
    long sessionCookieCount =
        setCookieHeaders.stream().filter(header -> header.startsWith("session=")).count();
    long csrfCookieCount =
        setCookieHeaders.stream().filter(header -> header.startsWith("csrf_token=")).count();

    assertEquals(1, sessionCookieCount);
    assertEquals(1, csrfCookieCount);
  }

  @Test
  void sessionPastAbsoluteCap_isRejectedEvenThoughExpiresAtStillValid() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();
    TestAuthHelper.setSessionExpiry(
        user.sessionCookie(),
        Instant.now().plus(Duration.ofDays(7)),
        Instant.now().minus(Duration.ofMinutes(1)));

    given().cookie("session", user.sessionCookie()).get("/api/users/me").then().statusCode(401);
  }
}
