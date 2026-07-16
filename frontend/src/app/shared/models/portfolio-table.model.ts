export interface PortfolioTableRow {
  id: string;
  name: string;
  customerName: string;
  projectLead: string | null;
  status: string;
  statusLabel: string;
  currentPhaseName: string | null;
  progressPercent: number;
  plannedEndDate: string;
  forecastEndDate: string | null;
  scheduleDeviationDays: number | null;
  budgetUtilizationPercent: number | null;
  budgetDeviationPercent: number | null;
  effortDeviationPercent: number | null;
  openRiskCount: number;
  criticalIssueCount: number;
  lastDataUpdate: string | null;
}

export interface PortfolioTable {
  projects: PortfolioTableRow[];
  empty: boolean;
}

export type PortfolioTableSortKey =
  | 'status'
  | 'progressPercent'
  | 'scheduleDeviationDays'
  | 'budgetDeviationPercent'
  | 'criticalIssueCount'
  | 'lastDataUpdate';
