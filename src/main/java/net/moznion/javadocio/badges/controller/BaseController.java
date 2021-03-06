package net.moznion.javadocio.badges.controller;

import java.nio.charset.StandardCharsets;

import me.geso.avans.ControllerBase;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;

public abstract class BaseController extends ControllerBase {
  @ResponseFilter
  public void securityFilters(WebResponse resp) {
    // Reducing MIME type security risks
    // http://msdn.microsoft.com/en-us/library/ie/gg622941(v=vs.85).aspx
    resp.addHeader("X-Content-Type-Options", "nosniff");

    // Avoid clickjacking attacks
    // (If you want to display this site in frames, remove this header)
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/X-Frame-Options
    resp.addHeader("X-Frame-Options", "DENY");

    // MUST NOT be cached by a shared cache, such as proxy server.
    resp.addHeader("Cache-Control", "private");
  }

  public WebResponse renderSvg(final String svgText) {
    if (svgText == null) {
      throw new IllegalArgumentException("svgText must not be null");
    }

    final byte[] bytes = svgText.getBytes(StandardCharsets.UTF_8);

    final ByteArrayResponse res = new ByteArrayResponse(200, bytes);
    res.setContentType("image/svg+xml");
    return res;
  }
}
