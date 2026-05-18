package com.macondo.jewelry.config;

import com.macondo.jewelry.payment.WompiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WompiProperties.class)
public class WompiConfig {
}
