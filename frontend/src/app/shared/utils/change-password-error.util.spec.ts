import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';

import { mapChangePasswordError } from './change-password-error.util';

describe('mapChangePasswordError', () => {
  it('maps BAD_CREDENTIALS', () => {
    expect(
      mapChangePasswordError(
        httpError(401, { code: 'BAD_CREDENTIALS', message: 'Current password is incorrect' }),
      ),
    ).toBe('Aktuelles Passwort ist falsch.');
  });

  it('maps min-length validation', () => {
    expect(
      mapChangePasswordError(
        httpError(400, {
          code: 'BAD_REQUEST',
          message: 'New password must be at least 8 characters',
        }),
      ),
    ).toBe('Das neue Passwort muss mindestens 8 Zeichen lang sein.');
  });

  it('maps same-as-current validation', () => {
    expect(
      mapChangePasswordError(
        httpError(400, {
          code: 'BAD_REQUEST',
          message: 'New password must differ from current password',
        }),
      ),
    ).toBe('Das neue Passwort muss sich vom aktuellen Passwort unterscheiden.');
  });
});

function httpError(status: number, body: { code: string; message: string }): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: body, statusText: 'Error' });
}
