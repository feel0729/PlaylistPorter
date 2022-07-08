package com.wei.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class TokenUtil {

  public String getToken(String tokenUrl, HttpHeaders headers,
      MultiValueMap<String, String> bodyParamMap, String tokenName) {
    RestTemplate restTemplate = new RestTemplate();
    String response = "";
    String token = "";

    try {
      HttpEntity<?> request = new HttpEntity<Object>(bodyParamMap, headers);
      response = restTemplate.postForObject(tokenUrl, request, String.class);
      token = new JSONObject(response).getString(tokenName);
    } catch (Exception e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      System.out.println(errors.toString());
      return "";
    }

    return token;
  }
}
