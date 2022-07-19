package com.wei.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class WebAsyncControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void porterCarryIsAlive() throws Exception {
    // test : porterCarry 活著
    this.mockMvc
        .perform(post("/porterCarry/KKBOX/Spotify?accessToken=test").param("accessToken", "test")
            .param("sourcePlaylist", "test").param("targetPlaylist", "test"))
        .andExpect(status().isOk()); // pass
  }
}
