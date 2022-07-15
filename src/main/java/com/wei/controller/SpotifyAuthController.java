package com.wei.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Scope("prototype")
public class SpotifyAuthController {
  private static final Logger logger = LogManager.getLogger();

  @Value("${SPOTIFY_CLIENT_ID}")
  private String spotifyClientId = "";

  @Value("${REDIRECT_URI_BASE}")
  private String redirectUriBase = "";

  @GetMapping("/spotifyAuthModifyPlaylist/{source}/{target}")
  public String spotifyAuthModifyPlaylist(@PathVariable(value = "source") String source,
      @PathVariable(value = "target") String target, Model model) {
    logger.info("spotifyAuthModifyPlaylist ...");
    model.addAttribute("redirectUri", redirectUriBase + "/porter/" + source + "/" + target);
    model.addAttribute("spotifyClientId", spotifyClientId);
    return "spotifyAuthModifyPlaylist";
  }
}
