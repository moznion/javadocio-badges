package net.moznion.javadocio.badges;

import org.postgresql.ds.PGPoolingDataSource;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Context class.
 * 
 * It must be singleton.
 * 
 * @author moznion
 *
 */
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

    PgConnectionInformation connInfo = PgConnectionInformation.parseUri(dbUri);

    PGPoolingDataSource dataSource = new PGPoolingDataSource();
    dataSource.setDataSourceName(dataSourceName);

    int port = connInfo.getPort();
    String host = connInfo.getHost();
    dataSource.setServerName(host + ":" + port);
    if (port < 0) { // if port is not specified, port is negative
      dataSource.setServerName(host);
    }
    dataSource.setDatabaseName(connInfo.getDbName());
    dataSource.setUser(connInfo.getUserName());
    dataSource.setPassword(connInfo.getPassword());
    dataSource.setMaxConnections(8);
  }
}
