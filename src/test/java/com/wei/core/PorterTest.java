package com.wei.core;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PorterTest {

  @Autowired
  Porter porter;

  @Test
  void porterReturnListNotNull() throws Exception {

    List<Map<String, String>> resultList = porter.carryPlaylist("", "", "", "", "");

    Assert.notNull(resultList, "List should not be null");
  }

}
