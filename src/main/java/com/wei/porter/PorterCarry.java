package com.wei.porter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.wei.playlist.KkboxPlaylist;
import com.wei.playlist.SpotifyPlaylist;
import com.wei.search.SpotifySearch;

@Component
public class PorterCarry {

  @Autowired
  KkboxPlaylist kkboxPlaylist;

  @Autowired
  SpotifySearch spotifySearch;

  @Autowired
  SpotifyPlaylist spotifyPlaylist;

  public List<Map<String, String>> kkboxToSpotify(String accessToken, String sourcePlaylistUrl,
      String targetPlaylistUrl) {
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

      Map<String, String> result = new HashMap<>();
      result.put("sourceSongIndex", "" + sourceSongIndex);
      result.put("sourceSongName", sourceSongName);
      result.put("sourceArtistName", sourceSong.get("artistName"));
      result.put("searchSongName", searchSongName.trim());
      result.put("targetSongUri", mostlikeResult.get("songUri"));
      result.put("targetSongName", mostlikeResult.get("songName"));
      result.put("targetArtistName", mostlikeResult.get("artistName"));
      result.put("carryResult", " ");
      resultList.add(result);
      sourceSongIndex++;
    }

    resultList = spotifyPlaylist.addItemToPlaylist(accessToken, targetPlaylistUrl, resultList);

    return resultList;
  }

}
