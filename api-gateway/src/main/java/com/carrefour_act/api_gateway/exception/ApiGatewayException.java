package com.carrefour_act.api_gateway.exception;

ApiGatewayException.java
package com.example.apigateway.exception;

public class ApiGatewayException extends RuntimeException {
 private static final long serialVersionUID = 1L;

 public ApiGatewayException(String message) {
 super(message);
 }

 public ApiGatewayException(String message, Throwable cause) {
 super(message, cause);
 }
}