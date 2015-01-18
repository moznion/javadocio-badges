package net.moznion.javadocio.badges.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import me.geso.tinyorm.TinyORM;

import net.moznion.capture.output.stream.StdoutCapturer;
import net.moznion.javadocio.badges.BadgeProvider;
import net.moznion.javadocio.badges.Context;
import net.moznion.javadocio.badges.row.Badge;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BadgeProviderTest {
  @BeforeClass
  public static void globalBefore() {
    new Context(); // XXX load singleton
  }

  @Before
  public void before() throws SQLException, IOException {
    TestUtil.initializeDatabase();
    System.setProperty("maximumLimitOfCacheRow", "1");
  }

  @Test
  public void cacheSuccessfullyWhenAccessed() throws URISyntaxException, IOException, SQLException {
    String groupId = "net.moznion";
    String artifactId = "mysql-diff";

    BadgeProvider badgeProvider = new BadgeProvider(groupId, artifactId);
    String svgString = badgeProvider.retrieve();
    assertTrue(svgString.startsWith("<svg"));

    try (Connection connection =
        PGPoolingDataSource.getDataSource(Context.dataSourceName).getConnection()) {
      TinyORM db = new TinyORM(connection);
      assertEquals("Cache successfully", db.count(Badge.class).execute(), 1);
    }

    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    try (StdoutCapturer stdoutCapturer = new StdoutCapturer(stdout)) {
      svgString = badgeProvider.retrieve();
    }
    Pattern pattern = Pattern.compile(".*Hit the cache.*", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(stdout.toString());
    assertTrue(matcher.find());
    assertTrue(svgString.startsWith("<svg"));
  }

  @Test
  public void limitingCacheRowSuccessfully() throws URISyntaxException, IOException, SQLException {
    BadgeProvider badgeProvider1 = new BadgeProvider("net.moznion", "mysql-diff");
    badgeProvider1.retrieve();
    BadgeProvider badgeProvider2 = new BadgeProvider("net.moznion", "mysql-namelocker");
    badgeProvider2.retrieve();

    try (Connection connection =
        PGPoolingDataSource.getDataSource(Context.dataSourceName).getConnection()) {
      TinyORM db = new TinyORM(connection);
      assertEquals("Limit successfully", db.count(Badge.class).execute(), 1);
    }
  }
}
