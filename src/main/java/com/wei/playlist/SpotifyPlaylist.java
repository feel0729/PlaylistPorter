package com.wei.playlist;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.wei.util.TokenUtil;

@Component
@Scope("prototype")
public class SpotifyPlaylist {
  private static final Logger logger = LogManager.getLogger();

  @Autowired
  TokenUtil tokenUtil;

  @Value("${SPOTIFY_CLIENT_ID}")
  private String clientId = "";

  @Value("${SPOTIFY_CLIENT_SECRET}")
  private String clientSecret = "";

  @Value("${SPOTIFY_REQUEST_DELAY_MILLISECONDS}")
  private long spotifyRequestDelayMilliseconds = 100; // DEFAULT VALUE

  private final String apiUrl = "https://api.spotify.com/v1/";

  public List<Map<String, String>> addItemToPlaylist(String accessToken, String playlistUrl,
      List<Map<String, String>> resultList) {

    String playlistId = getPlaylistId(playlistUrl);

    if (playlistId.isEmpty()) {
      return resultList;
    }

    if (accessToken != null && !accessToken.isEmpty()) {

      JSONArray uris = new JSONArray();

      for (Map<String, String> result : resultList) {
        String songUri = result.get("targetSongUri");
        if (songUri != null && !songUri.isEmpty()) {
          uris.put(songUri);
        }
      }

      String snapshotId = "";

      if (uris.length() > 0) {
        snapshotId = postAddItemToPlaylist(playlistId, accessToken, uris);
      }

      if (snapshotId != null && !snapshotId.isEmpty()) {
        resultList.forEach((m) -> {
          String songUri = m.get("targetSongUri");
          if (songUri != null && !songUri.isEmpty()) {
            m.put("carryResult", "Success !");
          }
        });
      }
    }
    return resultList;
  }

  private String postAddItemToPlaylist(String playlistId, String accessToken, JSONArray uris) {

    String searchUrl = apiUrl + "playlists/{playlistId}/tracks";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    JSONObject data = new JSONObject();

    try {
      data.put("uris", uris);
    } catch (JSONException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors.toString());
    }

    HttpEntity<?> request = new HttpEntity<Object>(data.toString(), headers);

    Map<String, Object> params = new HashMap<>();
    params.put("playlistId", playlistId);

    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<String> response = null;

    try {
      response = restTemplate.exchange(searchUrl, HttpMethod.POST, request, String.class, params);
    } catch (Exception e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors.toString());
    }

    String snapshotId = "";

    if (response != null) {

      JSONObject body = null;

      try {
        body = new JSONObject(response.getBody());
      } catch (JSONException e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        logger.error(errors.toString());
      }

      if (body.has("error")) {

        JSONObject error = null;
        try {
          error = body.getJSONObject("error");
        } catch (JSONException e) {
          StringWriter errors = new StringWriter();
          e.printStackTrace(new PrintWriter(errors));
          logger.error(errors.toString());
        }

        int status = 0;
        try {
          status = error.getInt("status");
        } catch (JSONException e) {
          StringWriter errors = new StringWriter();
          e.printStackTrace(new PrintWriter(errors));
          logger.error(errors.toString());
        }
        String msg = null;
        try {
          msg = error.getString("message");
        } catch (JSONException e) {
          StringWriter errors = new StringWriter();
          e.printStackTrace(new PrintWriter(errors));
          logger.error(errors.toString());
        }

        logger.info("reponse error status = " + status + " , message = " + msg);

        switch (status) {
          case 401:
            // Bad or expired token.
            break;
          case 429:
            // The app has exceeded its rate limits.
            try {
              TimeUnit.MILLISECONDS.sleep(spotifyRequestDelayMilliseconds);
            } catch (InterruptedException e) {
              StringWriter errors = new StringWriter();
              e.printStackTrace(new PrintWriter(errors));
              logger.error(errors.toString());
            }
            break;
        }
      } else {
        try {
          snapshotId = body.getString("snapshot_id");
        } catch (JSONException e) {
          StringWriter errors = new StringWriter();
          e.printStackTrace(new PrintWriter(errors));
          logger.error(errors.toString());
          return "";
        }
      }
    }
    return snapshotId;
  }

  private String getPlaylistId(String playListUrl) {

    String playlistId = "";

    // 歌單範例 https://open.spotify.com/playlist/xxxxxxxxx
    if (playListUrl.contains("open.spotify.com")) {
      if (playListUrl.contains("playlist") && playListUrl.contains("/")) {
        String[] pathVariables = playListUrl.split("/");
        for (int i = 0; i < pathVariables.length; i++) {
          if (pathVariables[i].equals("playlist")) {
            // playlistId 是 playlist 的下一個 pathVariable
            playlistId = pathVariables[i + 1];
            break; // 取到後就離開迴圈
          }
        }
      }
    }
    return playlistId;
  }
}
