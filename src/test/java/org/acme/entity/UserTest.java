package org.acme.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void hasRole_matchingSingleRole_returnsTrue() {
    User user = new User();
    user.setRole(User.Role.admin);

    assertTrue(user.hasRole(User.Role.admin));
  }

  @Test
  void hasRole_nonMatchingSingleRole_returnsFalse() {
    User user = new User();
    user.setRole(User.Role.customer);

    assertFalse(user.hasRole(User.Role.admin));
  }

  @Test
  void hasRole_matchesAnyOfMultipleRoles() {
    User user = new User();
    user.setRole(User.Role.support);

    assertTrue(user.hasRole(User.Role.admin, User.Role.support));
  }

  @Test
  void hasRole_matchesNoneOfMultipleRoles_returnsFalse() {
    User user = new User();
    user.setRole(User.Role.customer);

    assertFalse(user.hasRole(User.Role.admin, User.Role.support));
  }

  @Test
  void hasRole_noRoleSet_returnsFalse() {
    User user = new User();

    assertFalse(user.hasRole(User.Role.admin));
  }
}
