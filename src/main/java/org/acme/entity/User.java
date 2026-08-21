package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name = "User")
public class User extends PanacheEntity {

  // Since we're using PanacheEntity, the id field is provided automatically, so no need to define
  // userID manually.

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String email;

  @NotNull
  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "VARCHAR(20)")
  private Role role;

  public enum Role {
    CUSTOMER,
    ADMIN,
    VENDOR,
    SUPPORT
  }

  @Column(nullable = false)
  private int failedLoginAttempts = 0;

  private Instant lockedUntil;

  // Getters
  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public Role getRole() {
    return role;
  }

  public int getFailedLoginAttempts() {
    return failedLoginAttempts;
  }

  public Instant getLockedUntil() {
    return lockedUntil;
  }

  // Setters
  public void setUsername(String username) {
    this.username = username;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPassword(String password) {
    if (password != null && !password.isEmpty()) {
      this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }
  }

  public void setFailedLoginAttempts(int failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  public void setLockedUntil(Instant lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public boolean checkPassword(String password) {
    return BCrypt.checkpw(password, this.password);
  }

  // Method to check if the user has a specific role
  public boolean hasRole(
      Role... roles) { // Role... means that roles can be passed as multiple arguments
    for (Role role : roles) {
      if (this.role == role) {
        return true;
      }
    }
    return false;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  // The toString method is used to convert the object to a string representation.
  @Override
  public String toString() {
    return username + ", " + email;
  }
}
