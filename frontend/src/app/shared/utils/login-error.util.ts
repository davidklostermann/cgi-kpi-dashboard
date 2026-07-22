import { HttpErrorResponse } from '@angular/common/http';

import { ApiErrorBody } from '../models/auth.model';

const GENERIC_UNAVAILABLE =
  'Die Anmeldung ist derzeit nicht möglich. Bitte versuchen Sie es später erneut.';

/**
 * Maps login HTTP failures to safe, user-facing German messages.
 * Never surfaces raw backend `message` / stack traces.
 */
export function mapLoginError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return GENERIC_UNAVAILABLE;
  }

  if (error.status === 0) {
    return 'Der Anmeldedienst ist derzeit nicht erreichbar.';
  }

  const body = (error.error ?? null) as ApiErrorBody | null;
  if (body?.code === 'ACCOUNT_DISABLED') {
    return 'Ihr Konto ist deaktiviert.';
  }

  switch (error.status) {
    case 400:
      return 'Die Anmeldedaten sind unvollständig oder ungültig.';
    case 401:
    case 403:
      return 'Benutzername oder Passwort ist nicht korrekt.';
    case 404:
      return 'Die Anmeldung ist derzeit nicht verfügbar.';
    default:
      return GENERIC_UNAVAILABLE;
  }
}
