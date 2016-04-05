package net.moznion.javadocio.badges;

import lombok.extern.slf4j.Slf4j;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.tinyorm.TinyORM;

import net.moznion.javadocio.badges.exception.FailedFetchingBadgeException;
import net.moznion.javadocio.badges.row.Badge;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BadgeProvider {
  private final String groupId;
  private final String artifactId;
  private final String javadocVersion;

  private static final String baseUrl = "https://img.shields.io";
  private static final Mech2 mech2 = Mech2.builder().build();

  public BadgeProvider(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    javadocVersion = retrieveJavadocVersion();
  }

  public String retrieve() throws URISyntaxException, IOException, SQLException {
    try (Connection connection =
        PGPoolingDataSource.getDataSource(Context.dataSourceName).getConnection()) {
      TinyORM db = new TinyORM(connection);

      // Search cache
      Optional<String> maybeCachedSvg = searchCachedSvg(db);
      if (maybeCachedSvg.isPresent()) {
        return maybeCachedSvg.get();
      }

      String svgString = fetchSvgFromRemote();
      registerCache(svgString, db);
      return svgString;
    }
  }

  private long getCurrentUnixTime() {
    return System.currentTimeMillis() / 1000L;
  }

  private Optional<String> searchCachedSvg(TinyORM db) {
    Optional<Badge> maybeBadge = db.single(Badge.class)
        .where("group_id=?", groupId)
        .where("artifact_id=?", artifactId)
        .where("version=?", javadocVersion)
        .execute();

    if (maybeBadge.isPresent()) {
      log.debug(new StringBuilder()
          .append("Hit the cache (group_id: ")
          .append(groupId)
          .append(", artifact_id: ")
          .append(artifactId)
          .append(", version: ")
          .append(javadocVersion)
          .append(")")
          .toString());

      Badge badge = maybeBadge.get();

      // Update accessed at time stamp
      badge.update()
          .set("last_accessed_at", getCurrentUnixTime())
          .execute();

      return Optional.of(badge.getSvg());
    }

    return Optional.empty();
  }

  private String fetchSvgFromRemote() throws URISyntaxException, IOException {
    String shieldsIoUrl = buildShieldsIoUrl(javadocVersion);
    Mech2Result result = mech2.get(new URI(shieldsIoUrl)).execute();

    if (!result.isSuccess()) {
      log.warn(result.getResponse().getStatusLine().getReasonPhrase());
      throw new FailedFetchingBadgeException(shieldsIoUrl);
    }

    return result.getResponseBodyAsString();
  }

  private void registerCache(String svgString, TinyORM db) {
    Badge newBadge = new Badge();
    newBadge.setGroup_id(groupId);
    newBadge.setArtifact_id(artifactId);
    newBadge.setVersion(javadocVersion);
    newBadge.setSvg(svgString);

    Integer maximumLimitOfCacheRow = getMaximumLimitOfCacheRow();

    long numOfRow = db.count(Badge.class).execute();
    if (maximumLimitOfCacheRow != null && numOfRow >= maximumLimitOfCacheRow) {
      // If over limit rows, it overwrite a row which is not the most referenced
      db.single(Badge.class)
          .orderBy("last_accessed_at ASC")
          .limit(1)
          .execute().get()
          .update().setBean(newBadge).execute();
    } else {
      db.insert(Badge.class).valueByBean(newBadge).execute();
    }
  }

  /**
   * Fetch number of maximum rows for caching of badge from system property.
   * 
   * <p>
   * e.g. for Heroku Postgres Hobby Dev plan
   * https://devcenter.heroku.com/articles/heroku-postgres-plans#hobby-tier
   * </p>
   * 
   * @return Number of maximum rows for caching of badge. If it returns null, it means *UNLIMITED*.
   */
  private Integer getMaximumLimitOfCacheRow() {
    Integer maximumLimitOfCacheRow = null;
    String maximumLimitOfCacheRowTemp = System.getProperty("maximumLimitOfCacheRow");
    if (maximumLimitOfCacheRowTemp != null) {
      maximumLimitOfCacheRow = Integer.parseInt(maximumLimitOfCacheRowTemp, 10);
    }

    return maximumLimitOfCacheRow;
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
    String javadocIoUrl = JavadocIoUrlBuilder.build(groupId, artifactId);
    try {
      result = mech2.disableRedirectHandling() // to get URI even if redirecting
          .get(new URI(javadocIoUrl)).execute();
    } catch (Exception e) {
      log.warn(e.getMessage());
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
    if (paths.isEmpty()) {
      throw new FailedFetchingBadgeException(javadocIoUrl);
    }

    return paths.get(paths.size() - 1);
  }
}
