package net.moznion.javadocio.badges;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

@Slf4j
public class BadgeProvider {
  private final String groupId;
  private final String artifactId;

  private static final String baseUrl = "http://img.shields.io";
  private static final Mech2 mech2 = Mech2.builder().build();

  public BadgeProvider(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
  }

  public String fetch() throws URISyntaxException, IOException {
    String javadocVersion = retrieveJavadocVersion();
    String shieldsIoUrl = buildShieldsIoUrl(javadocVersion);

    Mech2Result result = mech2.get(new URI(shieldsIoUrl)).execute();
    if (!result.isSuccess()) {
      log.warn(result.getResponse().getStatusLine().getReasonPhrase());
      throw new FailedFetchingBadgeException(shieldsIoUrl);
    }
    return result.getResponseBodyAsString();
  }

  private String buildShieldsIoUrl(String javadocVersion) {
    return new StringBuilder()
        .append(baseUrl)
        .append("/badge/javadoc.io-")
        .append(javadocVersion)
        .append("-blue.svg?style=flat")
        .toString();
  }

  private String retrieveJavadocVersion() {
    Mech2Result result;
    try {
      result = mech2.disableRedirectHandling() // to get URI even if redirecting
          .get(new URI(JavadocIoUrlBuilder.build(groupId, artifactId))).execute();
    } catch (URISyntaxException | IOException e) {
      log.warn(e.getMessage());
      e.printStackTrace();
      return "Unknown";
    }

    HttpResponse res = result.getResponse();
    StatusLine statusLine = res.getStatusLine();

    if (statusLine.getStatusCode() >= 400) {
      log.warn(statusLine.getReasonPhrase());
      return "Unknown";
    }

    String location = res.getLastHeader("Location").getValue();
    List<String> paths = Arrays.asList(location.split("/"));
    return paths.get(paths.size() - 1);
  }
}
