package com.wei.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.wei.search.KkboxSearch;
import com.wei.search.SpotifySearch;

@Controller
public class MainController {

  @Autowired
  KkboxSearch kkboxSearch;

  @Autowired
  SpotifySearch spotifySearch;

  @GetMapping("/")
  public String main(Model model) {
    model.addAttribute("menuChoice", "homepage"); // 回傳首頁
    return "main";
  }

  @GetMapping("/search/{target}")
  public String search(@PathVariable String target, Model model) {
    model.addAttribute("menuChoice", "search"); // 回傳歌曲查詢頁
    model.addAttribute("target", target);
    return "main";
  }

  @GetMapping("/doSearch/{target}")
  public String doSearch(@PathVariable(value = "target") String target,
      @RequestParam(value = "keyword") String keyword, Model model) {
    model.addAttribute("menuChoice", "searchResult"); // 回傳查詢結果頁
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
}
