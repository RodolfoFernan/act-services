package com.carrefour_act.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

	@Bean
	public RouteLocator customRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("lancamentos-service", r -> r.path("/lancamentos/**").uri("http://localhost:8081")) // URL do
																											// serviço
																											// de
																											// lançamentos
				.route("outro-servico", r -> r.path("/outro-servico/**").uri("http://localhost:8082")) // URL do outro
																										// serviço
				.build();
	}
}
