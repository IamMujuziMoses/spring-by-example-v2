package com.springbyexample.v2.gateways.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

import com.springbyexample.v2.gateways.gateway.OrderProcessingGateway;

/**
 * Configuration for Spring Integration gateways.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EnableIntegration
@IntegrationComponentScan(basePackageClasses = OrderProcessingGateway.class)
public class GatewayConfiguration {
}
