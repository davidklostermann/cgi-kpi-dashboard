export interface PortfolioStatusDistribution {
  onTrack: number;
  atRisk: number;
  critical: number;
  completed: number;
}

export interface PortfolioKpiSummary {
  activeProjectCount: number;
  averageProgressPercent: number;
  budgetDeviationPercent: number;
  scheduleCompliancePercent: number;
  criticalRiskCount: number;
  statusDistribution: PortfolioStatusDistribution;
  empty: boolean;
}
