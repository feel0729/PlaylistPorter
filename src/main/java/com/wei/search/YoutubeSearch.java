package com.wei.search;

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
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.ThumbnailDetails;

@Component
@Scope("prototype")
public class YoutubeSearch {
  private static final Logger logger = LogManager.getLogger();

  @Value("${YOUTUBE_DEVELOPER_KEY}")
  private String DEVELOPER_KEY = "";

  private final String APPLICATION_NAME = "PlaylistPorterApiKeys";

  private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private final String BASE_URL = "https://www.youtube.com/watch?v=";

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

  public List<Map<String, String>> doSearch(String keyword) {
    try {
      return search(keyword);
    } catch (GeneralSecurityException | IOException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors.toString());
      return new ArrayList<>();
    }
  }

  private List<Map<String, String>> search(String keyword)
      throws GeneralSecurityException, IOException, GoogleJsonResponseException {
    List<Map<String, String>> resultList = new ArrayList<>();

    YouTube youtubeService = getService();

    YouTube.Search.List request = youtubeService.search().list("snippet");

    SearchListResponse response =
        request.setKey(DEVELOPER_KEY).setMaxResults(50L).setQ(keyword).setType("video").execute();

    logger.info("response=" + response.toString());

    List<SearchResult> searchResultItems = response.getItems();

    for (SearchResult searchResultItem : searchResultItems) {

      ResourceId resourceId = searchResultItem.getId();

      String videoId = resourceId.getVideoId();

      String songUrl = BASE_URL + videoId;

      SearchResultSnippet snippet = searchResultItem.getSnippet();

      String songName = snippet.getTitle();

      String channelTitle = snippet.getChannelTitle();

      ThumbnailDetails thumbnailDetails = snippet.getThumbnails();

      String imageUrl = thumbnailDetails.getDefault().getUrl();

      Map<String, String> result = new HashMap<>();
      result.put("songName", songName);
      result.put("songUrl", songUrl);
      result.put("albumName", channelTitle);
      result.put("imageUrl", imageUrl);
      result.put("artistName", "");
      resultList.add(result);
    }

    return resultList;
  }
}
