package org.acme.util;

import java.security.SecureRandom;
import java.util.Base64;

/** Utility class for generating secure random tokens. */
public final class TokenGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();

  private TokenGenerator() {}

  public static String generate() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
