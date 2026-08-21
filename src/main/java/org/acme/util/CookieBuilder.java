package org.acme.util;

import jakarta.ws.rs.core.NewCookie;

/** Builds the expired session/CSRF cookies used to clear a login on logout or account deletion. */
public final class CookieBuilder {

  private CookieBuilder() {}

  public static NewCookie expiredSessionCookie(boolean cookieSecure) {
    return new NewCookie.Builder("session")
        .value("")
        .path("/")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite(NewCookie.SameSite.LAX)
        .maxAge(0) // Expire the cookie immediately
        .build();
  }

  public static NewCookie expiredCsrfCookie(boolean cookieSecure) {
    return new NewCookie.Builder("csrf_token")
        .value("")
        .path("/")
        .httpOnly(false)
        .secure(cookieSecure)
        .sameSite(NewCookie.SameSite.LAX)
        .maxAge(0) // Expire the cookie immediately
        .build();
  }
}
