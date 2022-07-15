package com.wei.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.WebAsyncTask;
import com.wei.core.Porter;

@Controller
@Scope("prototype")
public class WebAsyncController {
  private static final Logger logger = LogManager.getLogger();

  @Autowired
  private Porter porter;

  @PostMapping("/porterCarry/{source}/{target}")
  public WebAsyncTask<String> porterCarry(@PathVariable(value = "source") String source,
      @PathVariable(value = "target") String target,
      @RequestParam(value = "accessToken") String accessToken,
      @RequestParam(value = "sourcePlaylist") String sourcePlaylistUrl,
      @RequestParam(value = "targetPlaylist") String targetPlaylistUrl, Model model) {

    model.addAttribute("menuChoice", "porterCarryResult"); // 回傳搬移結果頁

    String headline = source + " to " + target + " 搬移結果";
    model.addAttribute("headline", headline);

    model.addAttribute("source", source);
    model.addAttribute("target", target);

    model.addAttribute("headerMap", getPorterCarryHeaderMap(source, target));

    Long starttime = System.currentTimeMillis();

    WebAsyncTask<String> asyncTask = new WebAsyncTask<>(60 * 1000L, () -> {

      List<Map<String, String>> porterCarryResultList =
          porter.carryPlaylist(accessToken, source, target, sourcePlaylistUrl, targetPlaylistUrl);

      String usedTime =
          "耗時 " + (double) ((double) (System.currentTimeMillis() - starttime) / 1000.0) + " 秒";
      logger.info(usedTime);

      model.addAttribute("usedTime", usedTime);

      model.addAttribute("resultList", porterCarryResultList);
      return "main";
    });

    return asyncTask;
  }

  private Map<String, String> getPorterCarryHeaderMap(String source, String target) {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("sourceSongIndex", source + "歌單曲目");
    headerMap.put("sourceSongName", source + "歌曲名稱");
    headerMap.put("sourceArtistName", source + "歌手名稱");
    headerMap.put("searchSongName", "搜尋歌名");
    headerMap.put("targetSongName", target + "歌曲名稱");
    headerMap.put("targetArtistName", target + "歌手名稱");
    headerMap.put("likeLevel", "相似程度(數值越低越像)");
    headerMap.put("carryResult", target + "搬移結果");
    return headerMap;
  }
}
