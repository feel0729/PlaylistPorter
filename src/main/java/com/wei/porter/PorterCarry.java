package com.wei.porter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.wei.playlist.KkboxPlaylist;
import com.wei.playlist.SpotifyPlaylist;
import com.wei.search.SpotifySearch;

@Component
@Scope("prototype")
public class PorterCarry {
  private static final Logger logger = LogManager.getLogger();

  @Value("${SPOTIFY_REQUEST_DELAY_MILLISECONDS}")
  private long spotifyRequestDelayMilliseconds = 100; // DEFAULT VALUE

  @Autowired
  KkboxPlaylist kkboxPlaylist;

  @Autowired
  SpotifySearch spotifySearch;

  @Autowired
  SpotifyPlaylist spotifyPlaylist;

  public List<Map<String, String>> kkboxToSpotify(String accessToken, String sourcePlaylistUrl,
      String targetPlaylistUrl) {

    logger.info("kkboxToSpotify ... ");

    List<Map<String, String>> resultList = new ArrayList<>();

    List<Map<String, String>> sourcePlaylist = kkboxPlaylist.getPlaylist(sourcePlaylistUrl);

    int sourceSongIndex = 1;
    for (Map<String, String> sourceSong : sourcePlaylist) {

      String sourceSongName = sourceSong.get("songName");

      String sourceArtistName = sourceSong.get("artistName");

      String searchSongName = sourceSongName.trim();

      if (searchSongName.contains("-")) {
        searchSongName = searchSongName.substring(0, searchSongName.indexOf("-"));
      }
      if (searchSongName.contains("(")) {
        searchSongName = searchSongName.substring(0, searchSongName.indexOf("("));
      }
      if (searchSongName.contains("【")) {
        searchSongName = searchSongName.substring(0, searchSongName.indexOf("【"));
      }
      if (searchSongName.contains("（")) {
        searchSongName = searchSongName.substring(0, searchSongName.indexOf("（"));
      }

      Map<String, String> mostlikeResult =
          spotifySearch.doSearchMostLike(searchSongName, sourceArtistName);

      try {
        // Spotify’s API rate limit is calculated based on the number of calls
        // that your app makes to Spotify in a rolling 30 second window.
        TimeUnit.MILLISECONDS.sleep(spotifyRequestDelayMilliseconds);
      } catch (InterruptedException e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        logger.error(errors.toString());
      }

      Map<String, String> result = new HashMap<>();
      result.put("sourceSongIndex", "" + sourceSongIndex);
      result.put("sourceSongName", sourceSongName);
      result.put("sourceArtistName", sourceSong.get("artistName"));
      result.put("searchSongName", searchSongName.trim());
      result.put("targetSongUri", mostlikeResult.get("songUri"));
      result.put("targetSongName", mostlikeResult.get("songName"));
      result.put("targetArtistName", mostlikeResult.get("artistName"));
      result.put("carryResult", "");
      resultList.add(result);
      sourceSongIndex++;
    }

    return spotifyPlaylist.addItemToPlaylist(accessToken, targetPlaylistUrl, resultList);
  }

}
