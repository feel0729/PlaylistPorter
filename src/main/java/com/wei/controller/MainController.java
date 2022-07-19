package com.wei.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import com.wei.search.KkboxSearch;
import com.wei.search.SpotifySearch;
import com.wei.search.YoutubeSearch;

@Controller
@Scope("prototype")
public class MainController {
  private static final Logger logger = LogManager.getLogger();

  @Autowired
  KkboxSearch kkboxSearch;

  @Autowired
  SpotifySearch spotifySearch;

  @Autowired
  YoutubeSearch youtubeSearch;

  @GetMapping("/")
  public String main(Model model) {
    model.addAttribute("menuChoice", "homepage"); // 回傳首頁
    logger.info("main");
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
      @RequestParam(value = "keyword", required = true) String keyword, Model model) {

    model.addAttribute("menuChoice", "searchResult"); // 回傳查詢結果頁

    String headline = target + " " + keyword + " 查詢結果";
    model.addAttribute("headline", headline);

    model.addAttribute("target", target);
    model.addAttribute("keyword", keyword);

    Map<String, String> searchHeaderMap = getSearchHeaderMap(target);

    model.addAttribute("headerMap", searchHeaderMap);

    // 查資料
    List<Map<String, String>> resultList;

    switch (target.toUpperCase()) {
      case "KKBOX":
        resultList = kkboxSearch.doSearch(keyword);
        break;
      case "SPOTIFY":
        resultList = spotifySearch.doSearch(keyword);
        break;
      case "YOUTUBE":
        resultList = youtubeSearch.doSearch(keyword);
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
      case "YOUTUBE":
        sourcePlaceholder = "https://youtube.com/playlist?list=xxxxxxxxxxxxxxxxxx";
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
      case "YOUTUBE":
        sourcePlaceholder = "https://youtube.com/playlist?list=xxxxxxxxxxxxxxxxxx";
        break;
      default:
        targetPlaceholder = "";
        break;
    }
    model.addAttribute("targetPlaceholder", targetPlaceholder);

    return "main";
  }

  private Map<String, String> getSearchHeaderMap(String target) {
    Map<String, String> headerMap = new HashMap<>();
    String songName;
    String albumName;
    String imgUrl;
    String artistName;
    switch (target.toUpperCase()) {

      case "YOUTUBE":
        songName = "影片標題";
        albumName = "頻道名稱";
        imgUrl = "影片縮圖";
        artistName = "影片說明";
        break;
      case "KKBOX":
      case "SPOTIFY":
      default:
        songName = "歌曲名稱";
        albumName = "專輯名稱";
        imgUrl = "專輯封面";
        artistName = "歌手名稱";
        break;
    }
    headerMap.put("songName", songName);
    headerMap.put("albumName", albumName);
    headerMap.put("imgUrl", imgUrl);
    headerMap.put("artistName", artistName);
    return headerMap;
  }
}
