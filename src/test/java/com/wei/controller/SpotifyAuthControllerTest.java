package com.wei.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SpotifyAuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void returnHtml() throws Exception {
    // test : 回傳 網頁
    mockMvc.perform(get("/spotifyAuthModifyPlaylist/KKBOX/Spotify")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }

}
