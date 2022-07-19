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
public class MainControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void mainIsAlive() throws Exception {
    // test 1 : main 活著就好
    mockMvc.perform(get("/")).andExpect(status().isOk()); // pass
  }

  @Test
  void mainReturnHtml() throws Exception {
    // test 2 : main 回傳 網頁
    this.mockMvc.perform(get("/")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }

  @Test
  void searchReturnHtml() throws Exception {
    // test 3 : search 回傳 網頁
    this.mockMvc.perform(get("/search/KKBOX")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass

    this.mockMvc.perform(get("/search/Spotify")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }

  @Test
  void doSearchKkboxReturnHtml() throws Exception {
    // test 4 : doSearch kkbox 回傳 網頁
    this.mockMvc.perform(get("/doSearch/KKBOX?keyword=test")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass

  }

  @Test
  void doSearchSpotifyReturnHtml() throws Exception {
    // test 5 : doSearch spotify 回傳 網頁
    this.mockMvc.perform(get("/doSearch/Spotify?keyword=test")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }

  @Test
  void doSearchYoutubeReturnHtml() throws Exception {
    // test 6 : doSearch Youtube 回傳 網頁
    this.mockMvc.perform(get("/doSearch/Youtube?keyword=test")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }

  @Test
  void porterReturnHtml() throws Exception {
    // test 7 : porter 回傳 網頁
    this.mockMvc.perform(get("/porter/KKBOX/Spotify")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }
}
