package com.wei.playlist;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class KkboxPlaylistTest {

  @Autowired
  KkboxPlaylist kkboxPlaylist;

  @Test
  void getPlaylistReturnListNotNull() throws Exception {

    List<Map<String, String>> resultList = kkboxPlaylist.getPlaylist("");

    Assert.notNull(resultList, "List should not be null");
  }

  @Test
  void getPlaylistReturnListNotEmpty() throws Exception {

    List<Map<String, String>> resultList =
        kkboxPlaylist.getPlaylist("https://play.kkbox.com/playlist/Or5SWU8Q_b3P2FTzrU");

    Assert.notEmpty(resultList, "List should not be empty");
  }

}
