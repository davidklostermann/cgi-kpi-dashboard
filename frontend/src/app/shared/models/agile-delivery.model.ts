export type DeliveryMethod = 'AGILE' | 'HYBRID' | 'WATERFALL';

export interface AgileSprint {
  id: string;
  name: string;
  sequenceNo: number;
  lifecycle: 'PAST' | 'ACTIVE' | 'FUTURE';
  current: boolean;
  future: boolean;
  health: 'GOOD' | 'WATCH' | 'CRITICAL' | 'PLANNED';
  healthLabel: string;
  progressPercent: number;
  storyPointsPlanned: number;
  storyPointsCompleted: number;
  carryOverPoints: number;
  startDate: string;
  endDate: string;
}

export interface AgileSprintChart {
  sprintLabels: string[];
  plannedStoryPoints: number[];
  completedStoryPoints: number[];
  velocityTrend: Array<number | null>;
  futureFlags: boolean[];
}

export interface AgileDeliveryKpis {
  sprintHealth: string | null;
  sprintHealthLabel: string | null;
  totalStoryPoints: number;
  averageVelocity: number | null;
  carryOverNextSprint: number;
  openBlockerCount: number;
}

export interface AgileDelivery {
  projectId: string;
  deliveryMethod: DeliveryMethod;
  dataAvailable: boolean;
  emptyReason: string | null;
  dataSource: string;
  factsAsOf: string | null;
  sprints: AgileSprint[];
  chart: AgileSprintChart;
  kpis: AgileDeliveryKpis;
}
