export interface ProjectAiPriority {
  rank: number;
  title: string;
  reason: string;
  evidenceFactIds: string[];
}

export interface ProjectAiSuggestedAction {
  title: string;
  reason: string;
  suggestedOwner: string | null;
  suggestedDueDate: string | null;
  addressesType: string | null;
  addressesId: string | null;
  expectedEffect: string | null;
  evidenceFactIds: string[];
  isProposal: boolean;
}

export interface ProjectAiMissingData {
  area: string;
  description: string;
}

export interface ProjectAiAnalysis {
  projectId: string;
  factsAsOf: string;
  generatedAt: string;
  status: string;
  availableSources: string[];
  summary: string;
  priorities: ProjectAiPriority[];
  suggestedActions: ProjectAiSuggestedAction[];
  missingData: ProjectAiMissingData[];
  aiGenerated: boolean;
  disclaimer: string;
}

export interface ProjectAiQuestionResponse {
  answer: string;
  evidenceFactIds: string[];
  factsAsOf: string;
  generatedAt: string;
  insufficientEvidence: boolean;
  aiGenerated: boolean;
  disclaimer: string;
}

export interface ProjectAiDraft {
  title: string;
  body: string;
  owner: string;
  dueDate: string;
}
