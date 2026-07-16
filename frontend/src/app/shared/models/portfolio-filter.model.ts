export type PortfolioLifecycleFilter = '' | 'active' | 'completed' | 'all';

export interface PortfolioFilters {
  customer: string;
  projectLead: string;
  statuses: string[];
  phase: string;
  lifecycle: PortfolioLifecycleFilter;
  reportMonth: string;
  riskSeverity: string;
}

export interface PortfolioFilterOptions {
  customers: string[];
  projectLeads: string[];
  phases: string[];
  reportMonths: string[];
  statuses: string[];
  riskSeverities: string[];
}

export const EMPTY_PORTFOLIO_FILTERS: PortfolioFilters = {
  customer: '',
  projectLead: '',
  statuses: [],
  phase: '',
  lifecycle: '',
  reportMonth: '',
  riskSeverity: '',
};
