package org.acme.dto;

import org.acme.entity.User;

public class UpdateRoleDto {
  private User.Role role;

  public User.Role getRole() {
    return role;
  }

  public void setRole(User.Role role) {
    this.role = role;
  }
}
