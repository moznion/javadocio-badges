package net.moznion.javadocio.badge;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.webscrew.response.RedirectResponse;

import net.moznion.javadocio.badge.controller.BaseController;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class BadgeGenerator {
  private final String groupId;
  private final String artifactId;
  private final BaseController controller;

  private static final String baseUrl = "https://img.shields.io";
  private static final Mech2 mech2 = Mech2.builder().build();

  public BadgeGenerator(String groupId, String artifactId, BaseController controller) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.controller = controller;
  }

  public RedirectResponse generate() {
    return controller.redirect(buildShieldsIoUrl());
  }

  private String buildShieldsIoUrl() {
    return new StringBuilder()
        .append(baseUrl)
        .append("/badge/javadoc.io-")
        .append(retrieveJavadocVersion())
        .append("-blue.svg?style=flat")
        .toString();
  }

  private String retrieveJavadocVersion() {
    Mech2Result result;
    try {
      result = mech2.disableRedirectHandling() // to get URI even if redirecting
          .get(new URI(JavadocIoUrlBuilder.build(groupId, artifactId))).execute();
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      return "Unknown";
    }

    HttpResponse res = result.getResponse();
    StatusLine statusLine = res.getStatusLine();

    if (statusLine.getStatusCode() >= 400) {
      System.err.println(statusLine.getReasonPhrase());
      return "Unknown";
    }

    String location = res.getLastHeader("Location").getValue();
    List<String> paths = Arrays.asList(location.split("/"));
    return paths.get(paths.size() - 1);
  }
}
