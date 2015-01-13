package net.moznion.javadocio.badges.controller;

import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.PathParam;
import me.geso.webscrew.response.WebResponse;

import net.moznion.javadocio.badges.BadgeProvider;
import net.moznion.javadocio.badges.JavadocIoUrlBuilder;

public class RootController extends BaseController {
  @GET("/{groupId}/{artifactId}")
  public WebResponse getJavadocIo(@PathParam("groupId") final String groupId,
      @PathParam("artifactId") final String artifactId) {
    return this.redirect(JavadocIoUrlBuilder.build(groupId, artifactId));
  }

  @GET("/{groupId}/{artifactId}/badge.svg")
  public WebResponse getBadge(@PathParam("groupId") final String groupId,
      @PathParam("artifactId") final String artifactId) {
    return new BadgeProvider(groupId, artifactId, this).redirect();
  }
}
