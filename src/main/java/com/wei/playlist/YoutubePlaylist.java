package com.wei.playlist;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;

@Component
@Scope("prototype")
public class YoutubePlaylist {
  private final Logger logger = LogManager.getLogger();

  @Value("${YOUTUBE_DEVELOPER_KEY}")
  private String DEVELOPER_KEY = "";

  private final String APPLICATION_NAME = "PlaylistPorterApiKeys";

  private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  public YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME).build();
  }

  /**
   * Call function to create API service object. Define and execute API request. Print API response.
   *
   * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
   */
  public List<Map<String, String>> getPlaylistItems(String playlistId)
      throws GeneralSecurityException, IOException, GoogleJsonResponseException {
    List<Map<String, String>> resultList = new ArrayList<>();

    YouTube youtubeService = getService();
    // Define and execute the API request
    YouTube.PlaylistItems.List request =
        youtubeService.playlistItems().list("snippet,contentDetails");
    PlaylistItemListResponse response =
        request.setKey(DEVELOPER_KEY).setMaxResults(50L).setPlaylistId(playlistId).execute();

    List<PlaylistItem> playlistItems = response.getItems();

    Map<String, String> result;

    for (PlaylistItem playlistItem : playlistItems) {
      PlaylistItemSnippet snippet = playlistItem.getSnippet();
      String songName = snippet.getTitle();
      String artistName = snippet.getChannelTitle();

      logger.info("songName=" + songName);
      logger.info("artistName=" + artistName);

      result = new HashMap<>();
      result.put("songName", songName);
      result.put("artistName", artistName);
      resultList.add(result);
    }
    return resultList;
  }

  public List<Map<String, String>> getPlaylist(String playlistUrl) {

    String playlistId = getPlaylistId(playlistUrl);

    List<Map<String, String>> resultList = new ArrayList<>();

    try {
      resultList = getPlaylistItems(playlistId);
    } catch (GeneralSecurityException | IOException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors.toString());
      resultList = new ArrayList<>();
    }

    return resultList;
  }

  private String getPlaylistId(String playlistUrl) {

    logger.info("playlistUrl=" + playlistUrl);

    String playlistId = "";

    // 歌單網址範例
    // https://www.youtube.com/watch?v=uelHwf8o7_U&list=PL7D6D8EE8B346A645
    if (playlistUrl.contains("youtube.com")) {
      if (playlistUrl.contains("list=")) {
        playlistId = playlistUrl.substring(playlistUrl.lastIndexOf("list=") + 5);
        logger.info("playlistId=" + playlistId);
        if (playlistId.contains("&")) {
          playlistId = playlistId.substring(0, playlistId.indexOf("&"));
        }
      }
    }
    return playlistId;
  }
}
