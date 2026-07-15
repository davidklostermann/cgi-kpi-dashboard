package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.Risk;

public interface RiskRepository extends JpaRepository<Risk, UUID> {
}
