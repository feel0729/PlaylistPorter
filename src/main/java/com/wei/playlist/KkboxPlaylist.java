package com.wei.playlist;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.wei.util.TokenUtil;

@Component
@Scope("prototype")
public class KkboxPlaylist {
  private static final Logger logger = LogManager.getLogger();

  @Autowired
  TokenUtil tokenUtil;

  @Value("${KKBOX_CLIENT_ID}")
  private String clientId = "";

  @Value("${KKBOX_CLIENT_SECRET}")
  private String clientSecret = "";

  private String token = "";

  private final String tokenUrl = "https://account.kkbox.com/oauth2/token";
  private final String apiUrl = "https://api.kkbox.com/v1.1/";

  public List<Map<String, String>> getPlaylist(String playListUrl) {

    List<Map<String, String>> resultList = new ArrayList<>();

    String playlistId = getPlaylistId(playListUrl);

    if (playlistId.isEmpty()) {
      return resultList;
    }

    if (getToken()) {

      String searchUrl = apiUrl
          + "/shared-playlists/{playlistId}/tracks?territory={territory}&offset={offset}&limit={limit}";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);

      HttpEntity<?> request = new HttpEntity<Object>("", headers);

      Map<String, Object> params = new HashMap<>();
      params.put("playlistId", playlistId);
      params.put("territory", "TW");
      params.put("offset", 0);
      params.put("limit", 500);

      RestTemplate restTemplate = new RestTemplate();

      ResponseEntity<String> response = null;

      try {
        response = restTemplate.exchange(searchUrl, HttpMethod.GET, request, String.class, params);
      } catch (Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        logger.error(errors.toString());
      }

      if (response != null) {
        resultList = analyzeResponse(response);
      }
    }

    return resultList;
  }

  private String getPlaylistId(String playListUrl) {

    String playlistId = "";

    // 歌單網址範例 https://play.kkbox.com/playlist/xxxxxx
    if (playListUrl.contains("play.kkbox.com") || playListUrl.contains("www.kkbox.com")) {
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

  private List<Map<String, String>> analyzeResponse(ResponseEntity<String> response) {
    List<Map<String, String>> resultList = new ArrayList<>();

    try {
      JSONObject body = new JSONObject(response.getBody().toString());

      JSONArray data = body.getJSONArray("data");

      int size = data.length();

      for (int itemIndex = 0; itemIndex < size; itemIndex++) {
        JSONObject item = data.getJSONObject(itemIndex);

        // 歌曲名
        String songName = item.getString("name");

        // 歌手
        String artistName = item.getJSONObject("album").getJSONObject("artist").getString("name");

        Map<String, String> result = new HashMap<>();
        result.put("songIndex", "" + (itemIndex + 1));
        result.put("songName", songName);
        result.put("artistName", artistName);

        resultList.add(result);
      }

    } catch (Exception e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors.toString());
    }
    return resultList;
  }

  private boolean getToken() {

    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(clientId, clientSecret);

    MultiValueMap<String, String> bodyParamMap = new LinkedMultiValueMap<>();
    bodyParamMap.add("grant_type", "client_credentials");

    token = tokenUtil.getToken(tokenUrl, headers, bodyParamMap, "access_token");

    return !token.isEmpty();
  }

}
