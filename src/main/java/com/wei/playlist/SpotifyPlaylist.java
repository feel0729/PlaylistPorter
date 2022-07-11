package com.wei.playlist;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.wei.util.TokenUtil;

@Component
public class SpotifyPlaylist {

  @Autowired
  TokenUtil tokenUtil;

  @Value("${SPOTIFY_CLIENT_ID}")
  private String clientId = "";

  @Value("${SPOTIFY_CLIENT_SECRET}")
  private String clientSecret = "";

  private final String apiUrl = "https://api.spotify.com/v1/";

  public List<Map<String, String>> addItemToPlaylist(String accessToken, String playlistUrl,
      List<Map<String, String>> resultList) {

    String playlistId = getPlaylistId(playlistUrl);

    if (playlistId.isEmpty()) {
      return resultList;
    }

    List<Map<String, String>> newResultList = new ArrayList<>();

    if (accessToken != null && !accessToken.isEmpty()) {

      String searchUrl = apiUrl + "playlists/{playlistId}/tracks";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);

      JSONArray uris = new JSONArray();

      for (Map<String, String> result : resultList) {
        String songUri = result.get("targetSongUri");
        if (songUri != null && !songUri.isEmpty()) {
          uris.put(songUri);
        }
      }

      JSONObject data = null;

      try {
        data = new JSONObject().put("uris", uris);
      } catch (JSONException e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
      }

      System.out.println(data.toString());

      HttpEntity<?> request = new HttpEntity<Object>(data.toString(), headers);

      System.out.println(request.toString());

      Map<String, Object> params = new HashMap<>();
      params.put("playlistId", playlistId);

      RestTemplate restTemplate = new RestTemplate();

      ResponseEntity<String> response = null;

      try {
        response = restTemplate.exchange(searchUrl, HttpMethod.POST, request, String.class, params);
      } catch (Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        System.out.println(errors.toString());
      }

      if (response != null) {
        String snapshotId = "";
        try {
          snapshotId = new JSONObject(response.getBody()).getString("snapshot_id");
        } catch (JSONException e) {
          StringWriter errors = new StringWriter();
          e.printStackTrace(new PrintWriter(errors));
          System.out.println(errors.toString());
        }
        if (!snapshotId.isEmpty()) {
          for (Map<String, String> result : resultList) {
            if (!result.get("targetSongUri").isEmpty()) {
              result.put("carryResult", "Success !");
            }
            newResultList.add(result);
          }
        }
      }
    }

    return resultList;
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
