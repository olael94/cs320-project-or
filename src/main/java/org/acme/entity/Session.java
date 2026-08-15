package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "Session")
public class Session extends PanacheEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_user_id")
  public User user;

  @Column(nullable = false, unique = true)
  public String token;

  @Column(nullable = false)
  public String csrfToken;

  @Column(nullable = false)
  public Instant expiresAt;

  public static Session findValid(String token) {
    Session session = Session.find("token", token).firstResult();
    if (session == null || session.expiresAt.isBefore(Instant.now())) {
      return null;
    }
    return session;
  }
}
