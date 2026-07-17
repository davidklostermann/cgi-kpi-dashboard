export interface PortfolioTrendTopProject {
  projectId: string;
  projectName: string;
  reason: string;
  evidenceFactIds: string[];
}

export interface PortfolioTrendAnalysis {
  text: string;
  aiGenerated: boolean;
  disclaimer: string;
  generatedAt: string;
  topProjects: PortfolioTrendTopProject[];
}
