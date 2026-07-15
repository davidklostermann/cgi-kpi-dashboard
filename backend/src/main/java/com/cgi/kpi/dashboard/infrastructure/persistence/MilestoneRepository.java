package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.Milestone;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
}
