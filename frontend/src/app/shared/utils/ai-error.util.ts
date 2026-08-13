import { HttpErrorResponse } from '@angular/common/http';

export type AiPanelStatus = 'error' | 'disabled' | 'key_missing';

export interface AiPanelErrorView {
  status: AiPanelStatus;
  message: string;
  code: string | null;
}

interface ApiErrorBody {
  code?: string;
}

const AI_DISABLED_MESSAGE = 'Der KI-Assistent ist derzeit deaktiviert.';
const AI_KEY_MISSING_MESSAGE =
  'Für Ihren Benutzer ist noch kein KI-API-Key hinterlegt. Bitte hinterlegen Sie den API-Key unter KI-Einstellungen.';

export function resolveAiPanelError(error: unknown, fallback: string): AiPanelErrorView {
  if (!(error instanceof HttpErrorResponse)) {
    const raw = error instanceof Error ? error.message : String(error ?? '');
    if (/failed to fetch/i.test(raw)) {
      return { status: 'error', message: fallback, code: null };
    }
    return { status: 'error', message: fallback, code: null };
  }

  const body = (error.error ?? null) as ApiErrorBody | null;
  const code = body?.code?.trim() || null;
  if (code === 'AI_DISABLED') {
    return { status: 'disabled', message: AI_DISABLED_MESSAGE, code };
  }

  if (code === 'AI_KEY_MISSING') {
    return {
      status: 'key_missing',
      message: AI_KEY_MISSING_MESSAGE,
      code,
    };
  }

  if (code === 'AI_PROVIDER_ERROR' || code === 'AI_UNAVAILABLE') {
    return { status: 'error', message: fallback, code };
  }

  return { status: 'error', message: fallback, code };
}

/** Prevents internal API diagnostics from leaking into user-facing section states. */
export function resolveLoadError(_error: unknown, fallback: string): string {
  return fallback;
}

export interface AiFactReference {
  id: string;
  label: string;
}

const FACT_LABELS: Record<string, string> = {
  'project.status': 'Projektstatus',
  'project.lead': 'Projektleitung',
  'kpi.progressPercent': 'Projektfortschritt',
  'kpi.scheduleDeviationDays': 'Terminabweichung',
  'kpi.risks.openCount': 'Offene Risiken',
  'kpi.problems.openCount': 'Offene Probleme',
  'budget.planned': 'Geplantes Budget',
  'budget.actual': 'Aktueller Budgetverbrauch',
  'budget.forecastDeviation': 'Prognostizierte Budgetabweichung',
  'budget.forecastAtCompletion': 'Budgetprognose bei Abschluss',
  'report.progressDeltaPercent': 'Fortschritt seit dem letzten Bericht',
  'report.statusChange': 'Statusänderung seit dem letzten Bericht',
  'portfolio.budgetDeviationPercent': 'Budgetabweichung im Portfolio',
  'portfolio.criticalRiskCount': 'Kritische Risiken im Portfolio',
  'portfolio.averageProgressPercent': 'Durchschnittlicher Projektfortschritt',
  'portfolio.scheduleCompliancePercent': 'Termintreue im Portfolio',
};

const PREFIX_LABELS: Array<[string, string]> = [
  ['snapshot.scheduleDeviationDays', 'Terminabweichung'],
  ['snapshot.status', 'Projektstatus'],
  ['snapshot.openRiskCount', 'Offene Risiken'],
  ['snapshot.actualBudget', 'Aktueller Budgetverbrauch'],
  ['snapshot.progressPercent', 'Projektfortschritt'],
  ['risk.', 'Risiko'],
  ['problem.', 'Problem'],
  ['issue.', 'Handlungsbedarf'],
  ['phase.', 'Projektphase'],
  ['milestone.', 'Meilenstein'],
];

export function resolveAiFactReferences(
  factIds: string[] | null | undefined,
): AiFactReference[] {
  const candidates: AiFactReference[] = [];
  const seenIds = new Set<string>();

  for (const rawId of factIds ?? []) {
    const id = rawId?.trim();
    if (!id) {
      continue;
    }
    const label = resolveAiFactLabel(id);
    if (!label || seenIds.has(id)) {
      continue;
    }
    seenIds.add(id);
    candidates.push({ id, label });
  }

  const labelCounts = candidates.reduce((counts, reference) => {
    counts.set(reference.label, (counts.get(reference.label) ?? 0) + 1);
    return counts;
  }, new Map<string, number>());
  const labelIndexes = new Map<string, number>();

  return candidates.map((reference) => {
    if ((labelCounts.get(reference.label) ?? 0) === 1) {
      return reference;
    }
    const index = (labelIndexes.get(reference.label) ?? 0) + 1;
    labelIndexes.set(reference.label, index);
    return { ...reference, label: `${reference.label} ${index}` };
  });
}

export function resolveAiFactLabel(factId: string | null | undefined): string | null {
  const id = factId?.trim();
  if (!id) {
    return null;
  }
  return FACT_LABELS[id] ?? PREFIX_LABELS.find(([prefix]) => id.startsWith(prefix))?.[1] ?? null;
}
