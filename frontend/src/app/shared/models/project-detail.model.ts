export interface ProjectMasterData {
  id: string;
  name: string;
  customer: string;
  projectLead: string | null;
  startDate: string;
  plannedEndDate: string;
  forecastEndDate: string | null;
  currentPhaseName: string | null;
  status: string;
  statusLabel: string;
  deliveryMethod: 'AGILE' | 'HYBRID' | 'WATERFALL';
  deliveryMethodLabel: string;
  lastDataUpdate: string | null;
}
