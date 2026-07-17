import { HttpErrorResponse } from '@angular/common/http';

import { resolveAiPanelError } from './ai-error.util';

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
    expect(result.message).toBe('Projekt-Assistent ist deaktiviert.');
    expect(result.code).toBe('AI_DISABLED');
  });

  it('maps AI_PROVIDER_ERROR to error status with diagnostic code', () => {
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
    expect(result.message).toContain('Gemini-Authentifizierung fehlgeschlagen');
    expect(result.message).toContain('(AI_PROVIDER_ERROR)');
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
    expect(result.message).toContain('(AI_UNAVAILABLE)');
    expect(result.code).toBe('AI_UNAVAILABLE');
  });
});
