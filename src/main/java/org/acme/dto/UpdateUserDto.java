package org.acme.dto;

public class UpdateUserDto {
  private String username;
  private String email;

  // Getters and setters
  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
