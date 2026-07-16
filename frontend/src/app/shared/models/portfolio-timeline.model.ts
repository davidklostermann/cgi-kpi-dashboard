export interface PortfolioTimelinePhase {
  name: string;
  phaseType: string;
  startDate: string;
  endDate: string;
  sortOrder: number;
}

export interface PortfolioTimelineMilestone {
  name: string;
  dueDate: string;
  completedDate: string | null;
  status: string;
  statusLabel: string;
}

export interface PortfolioTimelineProject {
  id: string;
  name: string;
  startDate: string;
  plannedEndDate: string;
  forecastEndDate: string | null;
  actualEndDate: string | null;
  scheduleDeviationDays: number | null;
  status: string;
  statusLabel: string;
  phases: PortfolioTimelinePhase[];
  milestones: PortfolioTimelineMilestone[];
}

export interface PortfolioTimeline {
  projects: PortfolioTimelineProject[];
  empty: boolean;
}
