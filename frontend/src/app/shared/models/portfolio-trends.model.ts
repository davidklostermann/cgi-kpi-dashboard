import { PortfolioStatusDistribution } from './portfolio-kpi.model';

export interface PortfolioTrendPoint {
  period: string;
  averageProgressPercent: number;
  totalActualBudget: number;
}

export interface PortfolioTrends {
  points: PortfolioTrendPoint[];
  statusDistribution: PortfolioStatusDistribution;
  empty: boolean;
}

export type TrendHorizon = '3M' | '6M' | '12M';
