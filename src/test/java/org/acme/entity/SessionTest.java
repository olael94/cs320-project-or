package org.acme.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.time.Instant;
import org.acme.TestAuthHelper;
import org.acme.TestAuthHelper.AuthenticatedUser;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SessionTest {

  @Test
  void findValid_expiredSession_returnsNull() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();
    TestAuthHelper.setSessionExpiry(
        user.sessionCookie(),
        Instant.now().minus(Duration.ofMinutes(1)),
        Instant.now().plus(Duration.ofDays(30)));

    assertNull(TestAuthHelper.findValidSession(user.sessionCookie()));
  }

  @Test
  void findValid_pastAbsoluteCap_returnsNullEvenIfExpiresAtStillValid() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();
    TestAuthHelper.setSessionExpiry(
        user.sessionCookie(),
        Instant.now().plus(Duration.ofDays(7)),
        Instant.now().minus(Duration.ofMinutes(1)));

    assertNull(TestAuthHelper.findValidSession(user.sessionCookie()));
  }

  @Test
  void findValid_slidesExpiryForwardOnUse() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();
    TestAuthHelper.setSessionExpiry(
        user.sessionCookie(),
        Instant.now().plus(Duration.ofDays(1)),
        Instant.now().plus(Duration.ofDays(60)));

    Session session = TestAuthHelper.findValidSession(user.sessionCookie());
    assertNotNull(session);

    // Should have slid out to roughly now + IDLE_WINDOW (7 days), well past
    // the 1-day expiry it had before this call.
    Instant newExpiry = TestAuthHelper.getSessionExpiresAt(user.sessionCookie());
    assertTrue(newExpiry.isAfter(Instant.now().plus(Duration.ofDays(6))));
  }

  @Test
  void findValid_neverSlidesPastTheAbsoluteCap() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();
    Instant absoluteCap = Instant.now().plus(Duration.ofDays(2));
    TestAuthHelper.setSessionExpiry(
        user.sessionCookie(), Instant.now().plus(Duration.ofDays(1)), absoluteCap);

    TestAuthHelper.findValidSession(user.sessionCookie());

    // Clamped to the absolute cap, not the full 7-day idle window.
    Instant newExpiry = TestAuthHelper.getSessionExpiresAt(user.sessionCookie());
    assertEquals(absoluteCap.getEpochSecond(), newExpiry.getEpochSecond());
  }

  @Test
  void findValid_doesNotRewriteExpiryWithinTheSlideThreshold() {
    AuthenticatedUser user = TestAuthHelper.registerAndLogin();
    // Already within 30 minutes of what a fresh slide would set it to, so a
    // slide shouldn't happen - avoids a DB write on every single request.
    Instant almostFreshExpiry =
        Instant.now().plus(Duration.ofDays(7)).minus(Duration.ofMinutes(30));
    TestAuthHelper.setSessionExpiry(
        user.sessionCookie(), almostFreshExpiry, Instant.now().plus(Duration.ofDays(60)));

    TestAuthHelper.findValidSession(user.sessionCookie());

    Instant expiryAfter = TestAuthHelper.getSessionExpiresAt(user.sessionCookie());
    assertEquals(almostFreshExpiry.getEpochSecond(), expiryAfter.getEpochSecond());
  }
}
