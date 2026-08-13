import { HttpErrorResponse } from '@angular/common/http';

import { resolveAiPanelError, resolveAiFactReferences, resolveLoadError } from './ai-error.util';

describe('resolveAiPanelError', () => {
  it('maps AI_DISABLED to disabled status', () => {
    const result = resolveAiPanelError(
      new HttpErrorResponse({
        status: 503,
        error: { code: 'AI_DISABLED', message: 'Projekt-Assistent ist deaktiviert.' },
      }),
      'Fallback',
    );

    expect(result.status).toBe('disabled');
    expect(result.message).toBe('Der KI-Assistent ist derzeit deaktiviert.');
    expect(result.code).toBe('AI_DISABLED');
  });

  it('maps AI_PROVIDER_ERROR to a clean panel message', () => {
    const result = resolveAiPanelError(
      new HttpErrorResponse({
        status: 503,
        error: {
          code: 'AI_PROVIDER_ERROR',
          message: 'Gemini-Authentifizierung fehlgeschlagen. API-Key und Berechtigungen prüfen.',
        },
      }),
      'Fallback',
    );

    expect(result.status).toBe('error');
    expect(result.message).toBe('Fallback');
    expect(result.code).toBe('AI_PROVIDER_ERROR');
  });

  it('maps AI_UNAVAILABLE to error status', () => {
    const result = resolveAiPanelError(
      new HttpErrorResponse({
        status: 503,
        error: {
          code: 'AI_UNAVAILABLE',
          message: 'Der Projekt-Assistent ist derzeit nicht verfügbar.',
        },
      }),
      'Fallback',
    );

    expect(result.status).toBe('error');
    expect(result.message).toBe('Fallback');
    expect(result.code).toBe('AI_UNAVAILABLE');
  });

  it('maps AI_KEY_MISSING to key_missing status', () => {
    const result = resolveAiPanelError(
      new HttpErrorResponse({
        status: 403,
        error: {
          code: 'AI_KEY_MISSING',
          message:
            'Für Ihren Benutzer ist noch kein KI-API-Key hinterlegt. Bitte hinterlegen Sie den API-Key unter KI-Einstellungen.',
        },
      }),
      'Fallback',
    );

    expect(result.status).toBe('key_missing');
    expect(result.message).toContain('KI-API-Key');
    expect(result.code).toBe('AI_KEY_MISSING');
  });

  it('never exposes backend text for recognized AI states', () => {
    const result = resolveAiPanelError(
      new HttpErrorResponse({
        status: 403,
        error: { code: 'AI_KEY_MISSING', message: 'SQL column ai_key is null' },
      }),
      'Fallback',
    );

    expect(result.message).toContain('KI-API-Key');
    expect(result.message).not.toContain('SQL');
  });
});

describe('resolveLoadError', () => {
  it('returns only the approved user-facing fallback', () => {
    const internalError = { error: { message: 'SQL table project_snapshot missing' } };

    expect(resolveLoadError(internalError, 'Daten konnten nicht geladen werden.')).toBe(
      'Daten konnten nicht geladen werden.',
    );
  });
});

describe('resolveAiFactReferences', () => {
  it('maps technical fact ids to unique management labels', () => {
    expect(
      resolveAiFactReferences([
        'kpi.progressPercent',
        'risk.first.severity',
        'risk.second.status',
      ]),
    ).toEqual([
      { id: 'kpi.progressPercent', label: 'Projektfortschritt' },
      { id: 'risk.first.severity', label: 'Risiko 1' },
      { id: 'risk.second.status', label: 'Risiko 2' },
    ]);
  });

  it('hides unknown or empty technical ids', () => {
    expect(resolveAiFactReferences(['internal.unknownField', '', '   '])).toEqual([]);
  });
});
