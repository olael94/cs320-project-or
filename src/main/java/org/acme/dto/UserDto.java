package org.acme.dto;

import org.acme.entity.User;

/**
 * A Data Transfer Object (DTO) for the User entity. This class is used to transfer user data
 * between different layers of the application. It helps prevent exposing sensitive information,
 * such as passwords, and provides a simplified representation of the User entity.
 */
public class UserDto {
  public Long id;
  public String username;
  public String email;
  public User.Role role;
  public boolean active;

  public UserDto(User user) {
    this.id = user.id;
    this.username = user.getUsername();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.active = user.isActive();
  }
}
