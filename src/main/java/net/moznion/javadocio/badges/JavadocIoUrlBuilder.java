package net.moznion.javadocio.badges;

public class JavadocIoUrlBuilder {
  private static final String javadocIoBaseUrl = "http://www.javadoc.io/doc";

  public static String build(String groupId, String artifactId) {
    return new StringBuilder()
        .append(javadocIoBaseUrl)
        .append("/")
        .append(groupId)
        .append("/")
        .append(artifactId)
        .toString();
  }
}
