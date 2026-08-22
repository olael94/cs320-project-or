package org.acme.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void hasRole_matchingSingleRole_returnsTrue() {
    User user = new User();
    user.addRole(User.Role.ADMIN);

    assertTrue(user.hasRole(User.Role.ADMIN));
  }

  @Test
  void hasRole_nonMatchingSingleRole_returnsFalse() {
    User user = new User();
    user.addRole(User.Role.CUSTOMER);

    assertFalse(user.hasRole(User.Role.ADMIN));
  }

  @Test
  void hasRole_matchesAnyOfMultipleRoles() {
    User user = new User();
    user.addRole(User.Role.SUPPORT);

    assertTrue(user.hasRole(User.Role.ADMIN, User.Role.SUPPORT));
  }

  @Test
  void hasRole_matchesNoneOfMultipleRoles_returnsFalse() {
    User user = new User();
    user.addRole(User.Role.CUSTOMER);

    assertFalse(user.hasRole(User.Role.ADMIN, User.Role.SUPPORT));
  }

  @Test
  void hasRole_noRoleSet_returnsFalse() {
    User user = new User();

    assertFalse(user.hasRole(User.Role.ADMIN));
  }
}
