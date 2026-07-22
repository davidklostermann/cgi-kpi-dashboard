export type PortfolioInsightType =
  | 'SHARED_DEPENDENCY'
  | 'PHASE_VARIANCE'
  | 'CAPACITY_CONFLICT'
  | 'DETERIORATING_TREND'
  | 'MEASURE_INEFFECTIVENESS'
  | 'SYSTEMIC_RISK'
  | 'REPORTING_PATTERN';

export interface PortfolioInsightEvidence {
  label: string;
  value: string;
  projectId?: string | null;
  reportDate?: string | null;
  sourceField?: string | null;
}

export interface PortfolioInsight {
  id: string;
  type: PortfolioInsightType | string;
  title: string;
  finding: string;
  managementImplication: string;
  recommendedAction?: string | null;
  affectedProjectIds: string[];
  affectedProjectNames: string[];
  evidence: PortfolioInsightEvidence[];
  confidence: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  dataQuality: 'COMPLETE' | 'PARTIAL' | 'INSUFFICIENT' | string;
  detectedAt: string;
}

export interface PortfolioTrendAnalysis {
  insights: PortfolioInsight[];
  aiGenerated: boolean;
  disclaimer: string;
  generatedAt: string;
}
