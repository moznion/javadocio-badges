package net.moznion.javadocio.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.moznion.javadocio.badges.TestUtil;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;

import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.servlet.ServletException;

public class RootControllerTest {
  private static Mech2WithBase mech;
  private static Tomcat tomcat;

  @BeforeClass
  public static void before() throws ServletException, LifecycleException, URISyntaxException,
      SQLException, IOException {
    tomcat = new Tomcat();
    tomcat.setPort(0);
    org.apache.catalina.Context webContext = tomcat.addWebapp("/", new
        File("src/main/webapp").getAbsolutePath());
    webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR,
        "src/main/webapp/WEB-INF/web.xml");
    tomcat.start();

    int port = tomcat.getConnector().getLocalPort();
    String url = "http://127.0.0.1:" + port;
    mech = new Mech2WithBase(Mech2.builder().build(), new URI(url));

    TestUtil.initializeDatabase();
  }

  @AfterClass
  public static void after() throws Exception {
    if (tomcat != null) {
      tomcat.stop();
    }
  }

  @Test
  public void getJavadocIo() throws URISyntaxException, IOException {
    Mech2Result result = mech.get("/net.moznion/mysql-diff").execute();
    assertTrue(result.isSuccess());
  }

  @Test
  public void getBadge() throws URISyntaxException, IOException {
    Mech2Result result = mech.get("/net.moznion/mysql-diff/badge.svg").execute();
    assertTrue(result.isSuccess());
  }

  @Test
  public void notfoundBadge() throws URISyntaxException, IOException {
    Mech2Result result = mech.get("/naiyo.naiyo/sonnano.naiyo/badge.svg").execute();
    assertEquals(result.getResponse().getStatusLine().getStatusCode(), 404);

  }
}
