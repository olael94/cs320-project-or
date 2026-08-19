package org.acme.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter; // Import the filter interface
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider; //
import java.util.Set;
import org.acme.entity.Session;

@Provider // Marks this class as a JAX-RS provider (this means it will be automatically registered)
@Priority(Priorities.AUTHENTICATION) // AUTHENTICATION filters.
public class CsrfFilter implements ContainerRequestFilter {

  private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

  @Override
  public void filter(ContainerRequestContext requestContext) {
    // Check if the request method is safe (GET, HEAD, OPTIONS)
    if (SAFE_METHODS.contains(requestContext.getMethod())) {
      return;
    }

    // Check if the request contains a session cookie
    Cookie sessionCookie = requestContext.getCookies().get("session");
    if (sessionCookie == null) {
      return;
    }

    // Find the session by token
    Session session = Session.findValid(sessionCookie.getValue());
    if (session == null) {
      return;
    }

    // Check if the request contains a CSRF token
    String csrfHeader = requestContext.getHeaderString("X-CSRF-Token");
    if (csrfHeader == null || !csrfHeader.equals(session.csrfToken)) {
      requestContext.abortWith(
          Response.status(Response.Status.FORBIDDEN)
              .entity("Invalid or missing CSRF token")
              .build());
    }
  }
}
