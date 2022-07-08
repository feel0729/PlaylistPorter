package com.wei.search;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class SpotifySearch {

  @Autowired
  TokenUtil tokenUtil;

  @Value("${SPOTIFY_CLIENT_ID}")
  private String clientId = "";

  @Value("${SPOTIFY_CLIENT_SECRET}")
  private String clientSecret = "";

  private String token = "";

  private final String tokenUrl = "https://accounts.spotify.com/api/token";

  private final String apiUrl = "https://api.spotify.com/v1/";

  public List<Map<String, String>> doSearch(String keyword) {
    List<Map<String, String>> resultList = new ArrayList<>();

    if (getToken()) {

      String searchUrl = apiUrl
          + "/search?q={q}&type={type}&market={market}&offset={offset}&limit={limit}";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);

      HttpEntity<?> request = new HttpEntity<Object>("", headers);

      Map<String, Object> params = new HashMap<>();
      params.put("q", keyword);
      params.put("type", "track");
      params.put("market", "TW");
      params.put("offset", 0);
      params.put("limit", 10);

      RestTemplate restTemplate = new RestTemplate();

      ResponseEntity<String> response = null;

      try {
        response = restTemplate.exchange(searchUrl, HttpMethod.GET, request, String.class, params);
      } catch (Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        System.out.println(errors.toString());
      }

      if (response != null) {

        resultList = analyzeResponse(response);
      }
    }

    return resultList;
  }

  private List<Map<String, String>> analyzeResponse(ResponseEntity<String> response) {
    List<Map<String, String>> resultList = new ArrayList<>();

    try {
      JSONObject body = new JSONObject(response.getBody().toString());

      JSONObject tracks = body.getJSONObject("tracks");

      JSONArray items = tracks.getJSONArray("items");

      int size = items.length();

      // System.out.println("size=" + size);
      for (int itemIndex = 0; itemIndex < size; itemIndex++) {
        // System.out.println("i=" + i);
        JSONObject item = items.getJSONObject(itemIndex);

        // 歌曲名
        String songName = item.getString("name");

        // 歌曲連結
        String songUrl = item.getJSONObject("external_urls").getString("spotify");

        // 專輯
        JSONObject album = item.getJSONObject("album");
        String albumName = album.getString("name");

        // 專輯封面
        JSONArray images = album.getJSONArray("images");
        int imagesSize = images.length();
        long lastHeight = 0;
        String imageUrl = "";
        for (int imageIndex = 0; imageIndex < imagesSize; imageIndex++) {
          JSONObject image = images.getJSONObject(imageIndex);

          long height = image.getLong("height");

          if (lastHeight == 0 || height < lastHeight) {
            imageUrl = image.getString("url");
            lastHeight = height;
          }
        }

        // 歌手
        JSONArray artists = item.getJSONArray("artists");
        String artistName = "";
        for (int artistIndex = 0; artistIndex < artists.length(); artistIndex++) {
          artistName += (artistIndex == 0 ? "" : " / ")
              + artists.getJSONObject(artistIndex).getString("name");
        }

        Map<String, String> result = new HashMap<>();
        result.put("songName", songName);
        result.put("songUrl", songUrl);
        result.put("albumName", albumName);
        result.put("imageUrl", imageUrl);
        result.put("artistName", artistName);

        resultList.add(result);
      }

    } catch (Exception e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      System.out.println(errors.toString());
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