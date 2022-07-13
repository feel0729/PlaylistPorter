package com.wei.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import com.wei.porter.PorterCarry;
import com.wei.search.KkboxSearch;
import com.wei.search.SpotifySearch;

@Controller
@Scope("prototype")
public class MainController {

  @Autowired
  KkboxSearch kkboxSearch;

  @Autowired
  SpotifySearch spotifySearch;

  @Autowired
  PorterCarry porterCarry;

  @GetMapping("/")
  public String main(Model model) {
    model.addAttribute("menuChoice", "homepage"); // 回傳首頁
    return "main";
  }

  @GetMapping("/search/{target}")
  @RequestScope
  public String search(@PathVariable String target, Model model) {

    model.addAttribute("menuChoice", "search"); // 回傳歌曲查詢頁

    String headline = target + " 歌曲查詢";
    model.addAttribute("headline", headline);

    model.addAttribute("target", target);

    return "main";
  }

  @GetMapping("/doSearch/{target}")
  public String doSearch(@PathVariable(value = "target") String target,
      @RequestParam(value = "keyword") String keyword, Model model) {

    model.addAttribute("menuChoice", "searchResult"); // 回傳查詢結果頁

    String headline = target + " " + keyword + " 查詢結果";
    model.addAttribute("headline", headline);

    model.addAttribute("target", target);
    model.addAttribute("keyword", keyword);

    // 查資料
    List<Map<String, String>> resultList;

    switch (target.toUpperCase()) {
      case "KKBOX":
        resultList = kkboxSearch.doSearch(keyword);
        break;
      case "SPOTIFY":
        resultList = spotifySearch.doSearch(keyword);
        break;
      default:
        resultList = new ArrayList<>();
        break;
    }

    model.addAttribute("resultList", resultList);

    return "main";
  }

  @GetMapping("/porter/{source}/{target}")
  public String porter(@PathVariable(value = "source") String source,
      @PathVariable(value = "target") String target, Model model) {

    model.addAttribute("menuChoice", "porter"); // 回傳歌單搬移頁

    String headline = source + " to " + target + " 歌單搬移";
    model.addAttribute("headline", headline);

    model.addAttribute("source", source);
    model.addAttribute("target", target);

    String sourcePlaceholder = "";
    switch (source.toUpperCase()) {
      case "KKBOX":
        sourcePlaceholder = "https://play.kkbox.com/playlist/xxxxxxxxxxxxxxxxxx";
        break;
      case "SPOTIFY":
        sourcePlaceholder = "https://open.spotify.com/playlist/xxxxxxxxxxxxxxxxxxxxxx";
        break;
      default:
        sourcePlaceholder = "";
        break;
    }
    model.addAttribute("sourcePlaceholder", sourcePlaceholder);
    String targetPlaceholder = "";
    switch (target.toUpperCase()) {
      case "KKBOX":
        targetPlaceholder = "https://play.kkbox.com/playlist/xxxxxxxxxxxxxxxxxx";
        break;
      case "SPOTIFY":
        targetPlaceholder = "https://open.spotify.com/playlist/xxxxxxxxxxxxxxxxxxxxxx";
        break;
      default:
        targetPlaceholder = "";
        break;
    }
    model.addAttribute("targetPlaceholder", targetPlaceholder);

    return "main";
  }

  @PostMapping("/porterCarry/{source}/{target}")
  @Transactional(timeout = 300)
  public String porterCarry(@PathVariable(value = "source") String source,
      @PathVariable(value = "target") String target,
      @RequestParam(value = "accessToken") String accessToken,
      @RequestParam(value = "sourcePlaylist") String sourcePlaylist,
      @RequestParam(value = "targetPlaylist") String targetPlaylist, Model model) {

    Long starttime = System.currentTimeMillis();

    model.addAttribute("menuChoice", "porterCarryResult"); // 回傳搬移結果頁

    String headline = source + " to " + target + " 搬移結果";
    model.addAttribute("headline", headline);

    model.addAttribute("source", source);
    model.addAttribute("target", target);
    // model.addAttribute("sourcePlaylist", sourcePlaylist);
    // model.addAttribute("targetPlaylist", targetPlaylist);

    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("sourceSongIndex", source + "歌單曲目");
    headerMap.put("sourceSongName", source + "歌曲名稱");
    headerMap.put("sourceArtistName", source + "歌手名稱");
    headerMap.put("searchSongName", "搜尋歌名");
    headerMap.put("targetSongName", target + "歌曲名稱");
    headerMap.put("targetArtistName", target + "歌手名稱");
    headerMap.put("carryResult", target + "搬移結果");
    model.addAttribute("headerMap", headerMap);

    // 查資料
    List<Map<String, String>> resultList;

    if (source.toUpperCase().equals("KKBOX") && target.toUpperCase().equals("SPOTIFY")) {
      resultList = porterCarry.kkboxToSpotify(accessToken, sourcePlaylist, targetPlaylist);
    } else if (source.toUpperCase().equals("SPOTIFY") && target.toUpperCase().equals("KKBOX")) {
      // TODO: Spotify to KKBOX
      resultList = new ArrayList<>();
    } else {
      resultList = new ArrayList<>();
    }

    model.addAttribute("resultList", resultList);

    String usedTime =
        "耗時 " + (double) ((double) (System.currentTimeMillis() - starttime) / 1000.0) + " 秒";

    model.addAttribute("usedTime", usedTime);

    return "main";
  }

  @GetMapping("/spotifyAuthModifyPlaylist")
  public String spotifyAuthModifyPlaylist(Model model) {
    return "spotifyAuthModifyPlaylist";
  }
}
