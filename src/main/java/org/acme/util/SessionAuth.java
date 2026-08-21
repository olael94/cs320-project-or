package org.acme.util;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import org.acme.entity.Session;
import org.acme.entity.User;

/** Shared session/role checks reused across controllers. */
public final class SessionAuth {

  private SessionAuth() {}

  /** Returns the valid session for this cookie, or null if there isn't one. */
  public static Session requireValidSession(Cookie sessionCookie) {
    if (sessionCookie == null) {
      return null;
    }
    return Session.findValid(sessionCookie.getValue());
  }

  /** Returns null if the session's user has one of the given roles, or a 403 Response if not. */
  public static Response requireRole(Session session, User.Role... roles) {
    if (!session.user.hasRole(roles)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    return null;
  }
}
