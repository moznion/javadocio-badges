package net.moznion.javadocio.badges;

public class FailedFetchingBadgeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public FailedFetchingBadgeException(String BadgeUrl) {
    super("Failed to fetch badge from " + BadgeUrl);
  }
}
