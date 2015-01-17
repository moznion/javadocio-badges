package net.moznion.javadocio.badges;

import lombok.extern.slf4j.Slf4j;

import org.postgresql.ds.PGPoolingDataSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Context class.
 * 
 * It must be singleton.
 * 
 * @author moznion
 *
 */
@Slf4j
public class Context {
  public static final String dataSourceName = "pg-datasource";

  static {
    URI dbUri = null;
    try {
      dbUri = new URI(System.getenv("DATABASE_URL"));
    } catch (URISyntaxException e) {
      e.printStackTrace();
      System.exit(1);
    }

    List<String> userInfo = Arrays.asList(dbUri.getUserInfo().split(":"));

    if (userInfo.size() < 1) {
      log.error("Too few user information");
      System.exit(1);
    }
    String username = userInfo.get(0);

    String password = null;
    if (userInfo.size() >= 2) {
      password = userInfo.get(1);
    }

    PGPoolingDataSource dataSource = new PGPoolingDataSource();
    dataSource.setDataSourceName(dataSourceName);

    int port = dbUri.getPort();
    String host = dbUri.getHost();
    dataSource.setServerName(host + ":" + port);
    if (port < 0) {
      dataSource.setServerName(host);
    }

    dataSource.setDatabaseName(dbUri.getPath().substring(1));
    dataSource.setUser(username);
    dataSource.setPassword(password);
    dataSource.setMaxConnections(8);
  }
}
