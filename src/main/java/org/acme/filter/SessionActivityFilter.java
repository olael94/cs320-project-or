package org.acme.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.Provider;
import java.time.Duration;
import java.time.Instant;
import org.acme.entity.Session;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This filter checks for a valid session cookie on incoming requests and refreshes the session
 * cookie on outgoing responses if the session is still valid. It also sets the CSRF token cookie.
 */
@Provider
public class SessionActivityFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final String SESSION_PROPERTY = "acme.session";

  @Inject
  @ConfigProperty(name = "app.cookie.secure")
  boolean cookieSecure;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Cookie sessionCookie = requestContext.getCookies().get("session");
    if (sessionCookie == null) {
      return;
    }

    Session session = Session.findValid(sessionCookie.getValue());
    if (session != null) {
      requestContext.setProperty(SESSION_PROPERTY, session);
    }
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    // If the resource method already set its own Set-Cookie headers (login,
    // logout, delete-account all manage cookies explicitly), don't fight them.
    if (responseContext.getHeaders().containsKey("Set-Cookie")) {
      return;
    }

    Session session = (Session) requestContext.getProperty(SESSION_PROPERTY);
    if (session == null) {
      return;
    }

    long maxAge = Duration.between(Instant.now(), session.expiresAt).getSeconds();
    if (maxAge <= 0) {
      return;
    }

    NewCookie refreshedSession =
        new NewCookie.Builder("session")
            .value(session.token)
            .path("/")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge((int) maxAge)
            .build();

    NewCookie refreshedCsrf =
        new NewCookie.Builder("csrf_token")
            .value(session.csrfToken)
            .path("/")
            .httpOnly(false)
            .secure(cookieSecure)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge((int) maxAge)
            .build();

    responseContext.getHeaders().add("Set-Cookie", refreshedSession);
    responseContext.getHeaders().add("Set-Cookie", refreshedCsrf);
  }
}
