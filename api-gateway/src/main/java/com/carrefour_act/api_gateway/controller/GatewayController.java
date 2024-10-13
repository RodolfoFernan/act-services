package com.carrefour_act.api_gateway.controller;

ApiGatewayController.java
package com.example.apigateway.controller;

import com.example.apigateway.service.ApiGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiGatewayController {

 private final ApiGatewayService apiGatewayService;

 public ApiGatewayController(ApiGatewayService apiGatewayService) {
 this.apiGatewayService = apiGatewayService;
 }

 @GetMapping("/some-data")
 public ResponseEntity<String> getSomeData() {
 return apiGatewayService.getSomeData();
 }

 @PostMapping("/some-data")
 public ResponseEntity<String> postSomeData(@RequestBody String data) {
 return apiGatewayService.postSomeData(data);
 }
}