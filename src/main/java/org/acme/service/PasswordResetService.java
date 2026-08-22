package org.acme.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.acme.entity.PasswordResetToken;
import org.acme.entity.User;
import org.acme.util.TokenGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared between UserController's self-service password reset and AdminController's admin-triggered
 * one - both need the same token-creation and email logic, just a different intro line.
 */
@ApplicationScoped
public class PasswordResetService {

  private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

  @Inject Mailer mailer;

  @Inject
  @ConfigProperty(name = "app.base-url")
  String baseUrl;

  public void sendPasswordResetEmail(User user, String introText) {
    // Invalidate any previous unused reset tokens for this user, so only
    // the newest link is ever valid.
    PasswordResetToken.update("used = true where user = ?1 and used = false", user);

    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.user = user;
    resetToken.token = TokenGenerator.generate();
    resetToken.expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
    resetToken.persist();

    String resetLink = baseUrl + "/reset-password?token=" + resetToken.token;
    mailer.send(
        Mail.withText(
            user.getEmail(),
            "Reset your password",
            introText
                + "Click the link below to reset your password. This link expires in 1 hour.\n\n"
                + resetLink));
    logger.info("Password reset email sent to: {}", user.getEmail());
  }
}
