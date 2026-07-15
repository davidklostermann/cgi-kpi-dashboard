package com.cgi.kpi.dashboard.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.cgi.kpi.dashboard.domain.model")
@EnableJpaRepositories(basePackages = "com.cgi.kpi.dashboard.infrastructure.persistence")
public class JpaConfig {
}
