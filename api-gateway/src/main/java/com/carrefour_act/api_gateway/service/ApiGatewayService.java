package com.carrefour_act.api_gateway.service;

import org.springframework.http.ResponseEntity;

public interface ApiGatewayService {
 ResponseEntity<String> getSomeData();
 ResponseEntity<String> postSomeData(String data);
 // Adicione mais métodos conforme necessário
}
