package org.acme.dto;

public class ChangePasswordDto {
  private String currentPassword;
  private String newPassword;

  // Getters and setters
  public String getCurrentPassword() {
    return currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
