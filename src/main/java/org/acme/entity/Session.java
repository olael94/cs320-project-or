package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "Session")
public class Session extends PanacheEntity {

  // Idle window: how long a session stays valid with no activity.
  public static final Duration IDLE_WINDOW = Duration.ofDays(7);
  // Absolute cap: hard limit on total session lifetime, even if constantly active.
  public static final Duration ABSOLUTE_WINDOW = Duration.ofDays(30);
  // Only bother extending (and writing to the DB) if it's been at least this
  // long since the last extension - avoids a write on every single request.
  private static final Duration SLIDE_THRESHOLD = Duration.ofHours(1);

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_user_id")
  public User user;

  @Column(nullable = false, unique = true)
  public String token;

  @Column(nullable = false)
  public String csrfToken;

  @Column(nullable = false)
  public Instant expiresAt;

  @Column(nullable = false)
  public Instant absoluteExpiresAt;

  public static Session findValid(String token) {
    Session session = Session.find("token", token).firstResult();
    if (session == null) {
      return null;
    }

    Instant now = Instant.now();
    if (session.expiresAt.isBefore(now) || session.absoluteExpiresAt.isBefore(now)) {
      return null;
    }

    // Sliding expiry: push expiresAt forward on activity, but never past the
    // absolute cap, and only if it's actually been a while since the last slide.
    Instant newExpiry = now.plus(IDLE_WINDOW);
    if (newExpiry.isAfter(session.absoluteExpiresAt)) {
      newExpiry = session.absoluteExpiresAt;
    }
    if (newExpiry.isAfter(session.expiresAt.plus(SLIDE_THRESHOLD))) {
      Instant slidExpiry = newExpiry;
      // A targeted update by id, not session.persist() - session was loaded
      // outside this new transaction, so Hibernate would see it as a
      // detached entity with an existing id and reject the persist().
      QuarkusTransaction.requiringNew()
          .run(() -> Session.update("expiresAt = ?1 where id = ?2", slidExpiry, session.id));
      session.expiresAt = slidExpiry;
    }

    return session;
  }
}
