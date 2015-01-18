package net.moznion.javadocio.badges;

import lombok.Getter;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Getter
class PgConnectionInformation {
  private final String userName;
  private final String password;
  private final int port;
  private final String host;
  private final String dbName;

  private PgConnectionInformation(String userName, String password, int port, String host,
      String dbName) {
    this.userName = userName;
    this.password = password;
    this.port = port;
    this.host = host;
    this.dbName = dbName;
  }

  public static PgConnectionInformation parseUri(URI dbUri) {
    List<String> userInfo = Arrays.asList(dbUri.getUserInfo().split(":"));

    if (userInfo.size() < 1) {
      throw new IllegalArgumentException("Too few user information");
    }

    String username = userInfo.get(0);
    String password = null;
    if (userInfo.size() >= 2) {
      password = userInfo.get(1);
    }
    int port = dbUri.getPort();
    String host = dbUri.getHost();
    String dbName = dbUri.getPath().substring(1);

    return new PgConnectionInformation(username, password, port, host, dbName);
  }
}
