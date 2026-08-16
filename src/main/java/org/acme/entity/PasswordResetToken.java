package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class PasswordResetToken extends PanacheEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_user_id")
  public User user;

  @Column(nullable = false, unique = true)
  public String token;

  @Column(nullable = false)
  public Instant expiresAt;

  @Column(nullable = false)
  public boolean used = false;

  // Find a valid password reset token
  public static PasswordResetToken findValid(String token) {
    PasswordResetToken resetToken = PasswordResetToken.find("token", token).firstResult();
    if (resetToken == null || resetToken.used || resetToken.expiresAt.isBefore(Instant.now())) {
      return null;
    }
    return resetToken;
  }
}
