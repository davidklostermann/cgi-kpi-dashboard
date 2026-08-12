package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.Problem;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {
}
