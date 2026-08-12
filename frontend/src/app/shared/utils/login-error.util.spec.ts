import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';

import { mapLoginError } from './login-error.util';

describe('mapLoginError', () => {
  it('maps network failure (status 0)', () => {
    expect(mapLoginError(new HttpErrorResponse({ status: 0 }))).toBe(
      'Der Anmeldedienst ist derzeit nicht erreichbar.',
    );
  });

  it('maps 400', () => {
    expect(mapLoginError(httpError(400, { code: 'BAD_REQUEST', message: 'Invalid request body' }))).toBe(
      'Die Anmeldedaten sind unvollständig oder ungültig.',
    );
  });

  it('maps 401 and 403 without raw backend message', () => {
    expect(
      mapLoginError(httpError(401, { code: 'BAD_CREDENTIALS', message: 'Invalid username or password' })),
    ).toBe('Benutzername oder Passwort ist nicht korrekt.');
    expect(mapLoginError(httpError(403, { code: 'FORBIDDEN', message: 'Forbidden' }))).toBe(
      'Benutzername oder Passwort ist nicht korrekt.',
    );
  });

  it('maps ACCOUNT_DISABLED on 403', () => {
    expect(mapLoginError(httpError(403, { code: 'ACCOUNT_DISABLED', message: 'Account is disabled' }))).toBe(
      'Ihr Konto ist deaktiviert.',
    );
  });

  it('maps 404 without raw Resource not found', () => {
    const message = mapLoginError(
      httpError(404, { code: 'NOT_FOUND', message: 'Resource not found' }),
    );
    expect(message).toBe('Die Anmeldung ist derzeit nicht verfügbar.');
    expect(message).not.toContain('Resource not found');
  });

  it('maps 5xx and unknown without raw backend message', () => {
    expect(mapLoginError(httpError(500, { code: 'INTERNAL_ERROR', message: 'An unexpected error occurred' }))).toBe(
      'Die Anmeldung ist derzeit nicht möglich. Bitte versuchen Sie es später erneut.',
    );
    expect(mapLoginError(httpError(418, { code: 'TEAPOT', message: 'I am a teapot' }))).toBe(
      'Die Anmeldung ist derzeit nicht möglich. Bitte versuchen Sie es später erneut.',
    );
    expect(mapLoginError(new Error('boom'))).toBe(
      'Die Anmeldung ist derzeit nicht möglich. Bitte versuchen Sie es später erneut.',
    );
  });
});

function httpError(status: number, body: { code: string; message: string }): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: body, statusText: 'Error' });
}
