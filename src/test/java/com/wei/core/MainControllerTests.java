package com.wei.core;

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

    // test 1 : mainIsAlive 活著就好
    mockMvc.perform(get("/")).andExpect(status().isOk()); // pass
  }

  @Test
  void mainIsHtml() throws Exception {

    // test 2 : mainIsHtml 是個網頁
    this.mockMvc.perform(get("/")).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8")); // pass
  }
}
