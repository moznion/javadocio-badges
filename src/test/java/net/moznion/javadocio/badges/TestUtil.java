package net.moznion.javadocio.badges;

import lombok.extern.slf4j.Slf4j;

import net.moznion.javadocio.badges.Context;

import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class TestUtil {
  public static void initializeDatabase() throws SQLException, IOException {
    try (Connection connection =
        PGPoolingDataSource.getDataSource(Context.dataSourceName).getConnection()) {
      try {
        connection.prepareStatement("DROP TABLE badge").executeUpdate();
      } catch (PSQLException e) {
        log.info(e.getMessage());
      }
      List<String> schema = Files.readAllLines(Paths.get("src/main/resources/sql/init.db"));
      connection.prepareStatement(String.join("", schema)).executeUpdate();
    }
  }
}
