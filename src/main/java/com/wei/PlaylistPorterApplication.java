package com.wei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.wei.core"})
public class PlaylistPorterApplication {

  public static void main(String[] args) {
    SpringApplication.run(PlaylistPorterApplication.class, args);
  }

}
