package net.moznion.javadocio.badges;

import lombok.extern.slf4j.Slf4j;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.tinyorm.TinyORM;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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

  private long getCurrentUnixTime() {
    return System.currentTimeMillis() / 1000L;
  }

  public String fetch() throws URISyntaxException, IOException, SQLException {
    try (Connection connection =
        PGPoolingDataSource.getDataSource(Context.dataSourceName).getConnection()) {
      TinyORM db = new TinyORM(connection);

      String javadocVersion = retrieveJavadocVersion();
      {
        // Search cache
        // XXX tinyorm does not work certainly. I wonder why...
        PreparedStatement sth =
            connection
                .prepareStatement("SELECT * FROM badge WHERE group_id=? AND artifact_id=? AND version=?");
        sth.setString(1, groupId);
        sth.setString(2, artifactId);
        sth.setString(3, javadocVersion);
        ResultSet rs = sth.executeQuery();
        if (rs.next()) {
          PreparedStatement sthToUpdate = connection
              .prepareStatement("UPDATE badge SET last_accessed_at = "
                  + getCurrentUnixTime()
                  + " WHERE group_id=? AND artifact_id=? AND version=?");
          sthToUpdate.setString(1, groupId);
          sthToUpdate.setString(2, artifactId);
          sthToUpdate.setString(3, javadocVersion);
          sthToUpdate.executeUpdate();
          return rs.getString("svg"); // return cached SVG
        }
      }

      // Fetch SVG badge from remote
      String shieldsIoUrl = buildShieldsIoUrl(javadocVersion);
      Mech2Result result = mech2.get(new URI(shieldsIoUrl)).execute();
      if (!result.isSuccess()) {
        log.warn(result.getResponse().getStatusLine().getReasonPhrase());
        throw new FailedFetchingBadgeException(shieldsIoUrl);
      }
      String svgString = result.getResponseBodyAsString();

      Badge newBadge = new Badge();
      newBadge.setGroup_id(groupId);
      newBadge.setArtifact_id(artifactId);
      newBadge.setVersion(javadocVersion);
      newBadge.setSvg(svgString);
      newBadge.setLast_accessed_at(getCurrentUnixTime());

      long numOfRow = db.count(Badge.class).execute();
      if (numOfRow >= 10000) {
        db.single(Badge.class)
            .orderBy("last_accessed_at ASC")
            .limit(1)
            .execute().get()
            .update().setBean(newBadge).execute();
      } else {
        db.insert(Badge.class).valueByBean(newBadge).execute();
      }

      return svgString;
    }
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
