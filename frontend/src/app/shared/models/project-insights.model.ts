export interface ProjectInsightItem {
  code: string;
  statement: string;
  metrics: string;
  comparisonValue: string | null;
  period: string;
  rationale: string;
  type: string;
}

export interface ProjectInsights {
  projectId: string;
  insights: ProjectInsightItem[];
}

export interface ProjectTrends {
  projectId: string;
  comparisonAvailable: boolean;
  unavailableReason: string | null;
  previousSnapshotDate: string | null;
  currentSnapshotDate: string | null;
  progressDeltaPercent: number | null;
  budgetActualDelta: number | null;
  scheduleDeviationDeltaDays: number | null;
  previousStatus: string | null;
  previousStatusLabel: string | null;
  currentStatus: string | null;
  currentStatusLabel: string | null;
  openRiskCountDelta: number | null;
}
