export interface IssueActionMetric {
  label: string;
  value: string;
}

export interface RequiredDecision {
  decideWho: string;
  decideBy: string | null;
  impactIfDeferred: string;
}

export interface IssueActionItem {
  id: string;
  itemType: 'PROBLEM' | 'RISK' | string;
  itemTypeLabel: string;
  category: string;
  title: string;
  cause: string;
  impact: string | null;
  severity: string;
  severityLabel: string;
  priority: string;
  metrics: IssueActionMetric[];
  owner: string | null;
  dueDate: string | null;
  overdueDays: number | null;
  overdueLabel: string | null;
  nextAction: string | null;
  escalationNeeded: boolean;
  actionKind: string;
  actionLabel: string;
  actionText: string | null;
  requiredDecision: RequiredDecision | null;
}

export interface ProjectIssuesActions {
  projectId: string;
  factsBadge: string;
  factsAsOf: string;
  items: IssueActionItem[];
}

export interface RoleCapacityItem {
  id: string;
  roleName: string;
  requiredFte: number;
  availableFte: number;
  coveragePercent: number;
}

export interface CapacitySummary {
  missingFte: number;
  nextAvailabilityDate: string | null;
  overloadedRoles: number;
  externalOptions: number;
  impactHeadline: string;
  impactDetail: string;
}

export interface ProjectCapacity {
  projectId: string;
  factsAsOf: string;
  factsBadge: string;
  roles: RoleCapacityItem[];
  summary: CapacitySummary | null;
}
