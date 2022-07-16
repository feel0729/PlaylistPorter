package com.wei.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.wei.playlist.KkboxPlaylist;
import com.wei.playlist.SpotifyPlaylist;
import com.wei.playlist.YoutubePlaylist;
import com.wei.search.SpotifySearch;

@Service
@Scope("prototype")
public class Porter {

  @Value("${SPOTIFY_REQUEST_DELAY_MILLISECONDS}")
  private long spotifyRequestDelayMilliseconds = 100; // DEFAULT VALUE

  @Autowired
  KkboxPlaylist kkboxPlaylist;

  @Autowired
  SpotifySearch spotifySearch;

  @Autowired
  SpotifyPlaylist spotifyPlaylist;

  @Autowired
  YoutubePlaylist youtubePlaylist;

  public List<Map<String, String>> carryPlaylist(String accessToken, String source, String target,
      String sourcePlaylistUrl, String targetPlaylistUrl) {

    List<Map<String, String>> sourcePlaylist = getPlaylist(source, sourcePlaylistUrl);

    List<Map<String, String>> porterCarryList = getPorterCarryList(sourcePlaylist, target);

    List<Map<String, String>> carryResultList =
        carry(target, accessToken, targetPlaylistUrl, porterCarryList);

    return carryResultList;
  }

  private List<Map<String, String>> carry(String target, String accessToken,
      String targetPlaylistUrl, List<Map<String, String>> porterCarryList) {

    List<Map<String, String>> carryResultList;

    switch (target.toUpperCase()) {
      case "SPOTIFY":
        carryResultList =
            spotifyPlaylist.addItemToPlaylist(accessToken, targetPlaylistUrl, porterCarryList);
        break;
      default:
        carryResultList = new ArrayList<>();
        break;
    }

    return carryResultList;
  }


  private List<Map<String, String>> getPlaylist(String source, String sourcePlaylistUrl) {

    List<Map<String, String>> sourcePlaylist;

    switch (source.toUpperCase()) {
      case "KKBOX":
        sourcePlaylist = kkboxPlaylist.getPlaylist(sourcePlaylistUrl);
        break;
      case "YOUTUBE":
        sourcePlaylist = youtubePlaylist.getPlaylist(sourcePlaylistUrl);
      default:
        sourcePlaylist = new ArrayList<>();
        break;
    }

    return sourcePlaylist;
  }

  private List<Map<String, String>> getPorterCarryList(List<Map<String, String>> sourcePlaylist,
      String target) {

    List<Map<String, String>> porterCarryList = new ArrayList<>();

    for (Map<String, String> sourceItem : sourcePlaylist) {

      Map<String, String> mostlikeResult;

      switch (target.toUpperCase()) {
        case "SPOTIFY":
          mostlikeResult = spotifySearch.doSearchMostLike(sourceItem, 10);
          break;
        default:
          mostlikeResult = new HashMap<>();;
          break;
      }

      Map<String, String> porterCarryMap = new HashMap<>();
      porterCarryMap.put("sourceSongIndex", sourceItem.get("songIndex"));
      porterCarryMap.put("sourceSongName", sourceItem.get("songName"));
      porterCarryMap.put("sourceArtistName", sourceItem.get("artistName"));
      porterCarryMap.put("searchSongName", mostlikeResult.get("searchSongName"));
      porterCarryMap.put("targetSongUri", mostlikeResult.get("songUri"));
      porterCarryMap.put("targetSongName", mostlikeResult.get("songName"));
      porterCarryMap.put("targetArtistName", mostlikeResult.get("artistName"));
      porterCarryMap.put("likeLevel", mostlikeResult.get("likeLevel"));
      porterCarryMap.put("carryResult", "");
      porterCarryList.add(porterCarryMap);
    }
    return porterCarryList;
  }
}
