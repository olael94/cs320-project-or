package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
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

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role", columnDefinition = "VARCHAR(20)")
  private Set<Role> roles = new HashSet<>();

  public enum Role {
    CUSTOMER,
    ADMIN,
    VENDOR,
    SUPPORT
  }

  @Column(nullable = false)
  private int failedLoginAttempts = 0;

  private Instant lockedUntil;

  @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
  private boolean active = true;

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

  public Set<Role> getRoles() {
    return roles;
  }

  public int getFailedLoginAttempts() {
    return failedLoginAttempts;
  }

  public Instant getLockedUntil() {
    return lockedUntil;
  }

  public boolean isActive() {
    return active;
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

  public void addRole(Role role) {
    roles.add(role);
  }

  public void removeRole(Role role) {
    roles.remove(role);
  }

  public void setFailedLoginAttempts(int failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  public void setLockedUntil(Instant lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean checkPassword(String password) {
    return BCrypt.checkpw(password, this.password);
  }

  // Method to check if the user has a specific role
  public boolean hasRole(
      Role... requiredRoles) { // Role... means that roles can be passed as multiple arguments
    for (Role requiredRole : requiredRoles) {
      if (roles.contains(requiredRole)) {
        return true;
      }
    }
    return false;
  }

  // The toString method is used to convert the object to a string representation.
  @Override
  public String toString() {
    return username + ", " + email;
  }
}
