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

  private Map<String, String> uriMap = new HashMap<>();

  public List<Map<String, String>> addItemToPlaylist(String accessToken, String playlistUrl,
      List<Map<String, String>> porterCarryList) {

    String playlistId = getPlaylistId(playlistUrl);

    if (playlistId.isEmpty()) {
      logger.info("playlistId is empty. playlistUrl=" + playlistUrl);
      return porterCarryList;
    }

    if (accessToken == null || accessToken.isEmpty()) {
      logger.info("accessToken is empty.");
      return porterCarryList;
    }

    if (porterCarryList == null || porterCarryList.isEmpty()) {
      logger.info("porterCarryList is empty.");
      return porterCarryList;
    }

    JSONArray uris = new JSONArray();

    uriMap = new HashMap<>();

    for (Map<String, String> porterCarryMap : porterCarryList) {
      String songUri = porterCarryMap.get("targetSongUri");
      if (songUri != null && !songUri.isEmpty()) {
        uris.put(songUri);
        if (uris.length() == 100) {
          post(playlistId, accessToken, uris);
          uris = new JSONArray();
        }
      }
    }

    if (uris.length() > 0) {
      post(playlistId, accessToken, uris);
    }

    if (uriMap != null && !uriMap.isEmpty()) {
      porterCarryList.forEach((m) -> {
        String songUri = m.get("targetSongUri");
        if (songUri != null && !songUri.isEmpty() && uriMap.containsKey(songUri)) {
          m.put("carryResult", "Success ! snapshotId:" + uriMap.get(songUri));
        }
      });
    }
    return porterCarryList;
  }

  private void post(String playlistId, String accessToken, JSONArray uris) {

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
      TimeUnit.MILLISECONDS.sleep(spotifyRequestDelayMilliseconds);
    } catch (InterruptedException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors.toString());
    }

    response = restTemplate.exchange(searchUrl, HttpMethod.POST, request, String.class, params);

    String snapshotId = "";

    if (response != null) {
      try {
        snapshotId = analyzeResponse(response);
      } catch (JSONException | InterruptedException e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        logger.error(errors.toString());
      }
    }

    if (snapshotId != null && !snapshotId.isEmpty()) {
      for (int i = 0; i < uris.length(); i++) {
        String uri = null;
        try {
          uri = uris.getString(i);
        } catch (JSONException e) {
          StringWriter errors = new StringWriter();
          e.printStackTrace(new PrintWriter(errors));
          logger.error(errors.toString());
        }
        if (uri != null && !uri.isEmpty()) {
          uriMap.put(uri, snapshotId);
        }
      }
    }
  }

  private String analyzeResponse(ResponseEntity<String> response)
      throws JSONException, InterruptedException {

    String snapshotId = "";

    JSONObject body = new JSONObject(response.getBody());

    if (body.has("error")) {
      JSONObject error = body.getJSONObject("error");
      int status = error.getInt("status");
      String msg = error.getString("message");

      logger.info("reponse error status = " + status + " , message = " + msg);

      switch (status) {
        case 401:
          // Bad or expired token.
          break;
        case 429:
          // The app has exceeded its rate limits.
          TimeUnit.MILLISECONDS.sleep(spotifyRequestDelayMilliseconds);
          break;
      }
    } else {
      snapshotId = body.getString("snapshot_id");
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
