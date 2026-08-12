export interface IsoBenefitsCard {
  expectedBenefit: string;
  benefitUnit: string;
  realizedPercent: number;
  status: 'GREEN' | 'AMBER' | 'RED';
  statusLabel: string;
}

export interface IsoScopeCard {
  scopeStatus: string;
  deviations: string[];
  trend: 'IMPROVING' | 'STABLE' | 'DETERIORATING';
  trendLabel: string;
}

export interface IsoChangeRequestsCard {
  total: number;
  open: number;
  inReview: number;
  approved: number;
  impactSchedule: string;
  impactScheduleLabel: string;
  impactCost: string;
  impactCostLabel: string;
  impactScope: string;
  impactScopeLabel: string;
}

export interface IsoQualityCard {
  qualityStatus: string;
  openDefects: number;
  criticalDefects: number;
  testAcceptanceStatus: string;
  progressPercent: number;
}

export interface IsoStakeholdersCard {
  sponsorCustomer: string;
  stakeholderStatus: string;
  escalationStatus: string;
  lastSteeringDate: string | null;
}

export interface ProjectIsoManagement {
  projectId: string;
  dataAvailable: boolean;
  emptyReason: string | null;
  factsAsOf: string;
  benefits: IsoBenefitsCard | null;
  scope: IsoScopeCard | null;
  changeRequests: IsoChangeRequestsCard | null;
  quality: IsoQualityCard | null;
  stakeholders: IsoStakeholdersCard | null;
}
