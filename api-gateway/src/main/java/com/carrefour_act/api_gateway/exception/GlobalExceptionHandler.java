package com.carrefour_act.api_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(ApiGatewayException.class)
 public ResponseEntity<String> handleApiGatewayException(ApiGatewayException ex) {
 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
 }