import { PortfolioTimelineProject } from './portfolio-timeline.model';

export interface ProjectPhaseItem {
  name: string;
  phaseType: string;
  status: string;
  statusLabel: string;
  plannedStartDate: string;
  plannedEndDate: string;
  actualOrForecastStartDate: string | null;
  actualOrForecastEndDate: string | null;
  deviationDays: number | null;
  blockers: string | null;
  sortOrder: number;
}

export interface ProjectMilestoneItem {
  name: string;
  status: string;
  statusLabel: string;
  plannedDueDate: string;
  actualOrForecastDate: string | null;
  deviationDays: number | null;
  overdueDays: number | null;
  overdue: boolean;
  blockers: string | null;
}

export interface ProjectPhases {
  projectId: string;
  projectName: string;
  startDate: string;
  plannedEndDate: string;
  forecastEndDate: string | null;
  actualEndDate: string | null;
  scheduleDeviationDays: number | null;
  status: string;
  statusLabel: string;
  phases: ProjectPhaseItem[];
  milestones: ProjectMilestoneItem[];
  accessibilitySummary: string;
}

export function toGanttProject(phases: ProjectPhases): PortfolioTimelineProject {
  return {
    id: phases.projectId,
    name: phases.projectName,
    startDate: phases.startDate,
    plannedEndDate: phases.plannedEndDate,
    forecastEndDate: phases.forecastEndDate,
    actualEndDate: phases.actualEndDate,
    scheduleDeviationDays: phases.scheduleDeviationDays,
    status: phases.status,
    statusLabel: phases.statusLabel,
    phases: phases.phases.map((phase) => ({
      name: phase.name,
      phaseType: phase.phaseType,
      startDate: phase.plannedStartDate,
      endDate: phase.plannedEndDate,
      sortOrder: phase.sortOrder,
    })),
    milestones: phases.milestones.map((milestone) => ({
      name: milestone.name,
      dueDate: milestone.plannedDueDate,
      completedDate: milestone.actualOrForecastDate,
      status: milestone.status,
      statusLabel: milestone.statusLabel,
    })),
  };
}
