package com.carrefour_act.api_gateway.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiGatewayServiceImpl implements ApiGatewayService {

 private final RestTemplate restTemplate;

 public ApiGatewayServiceImpl(RestTemplate restTemplate) {
 this.restTemplate = restTemplate;
 }

 @Override
 public ResponseEntity<String> getSomeData() {
 String url = "http://service-url/api/data"; // URL do microsserviço
 return restTemplate.getForEntity(url, String.class);
 }

 @Override
 public ResponseEntity<String> postSomeData(String data) {
 String url = "http://service-url/api/data"; // URL do microsserviço
 return restTemplate.postForEntity(url, data, String.class);
 }
}
