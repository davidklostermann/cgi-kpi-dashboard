export interface ProjectKpis {
  projectId: string;
  status: string;
  statusLabel: string;
  progressPercent: number;
  currentPhaseName: string | null;
  schedule: {
    timeElapsedPercent: number | null;
    deviationDays: number | null;
    plannedEndDate: string | null;
    forecastEndDate: string | null;
    actualEndDate: string | null;
  };
  budget: {
    planned: number | null;
    actual: number | null;
    utilizationPercent: number | null;
    deviationPercent: number | null;
    remaining: number | null;
    forecastAtCompletion: number | null;
  };
  effort: {
    plannedDays: number | null;
    actualDays: number | null;
    deviationPercent: number | null;
    remainingDays: number | null;
    forecastAtCompletionDays: number | null;
  };
  risks: { openCount: number; criticalOpenCount: number };
  problems: { openCount: number; criticalOpenCount: number };
}
