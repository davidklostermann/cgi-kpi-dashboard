import { HttpErrorResponse } from '@angular/common/http';

import { ApiErrorBody } from '../models/auth.model';

/** Maps change-password HTTP failures to safe German UI messages. */
export function mapChangePasswordError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Passwort konnte nicht geändert werden.';
  }

  const body = (error.error ?? null) as ApiErrorBody | null;
  if (body?.code === 'BAD_CREDENTIALS') {
    return 'Aktuelles Passwort ist falsch.';
  }

  if (body?.code === 'BAD_REQUEST' && body.message) {
    return mapBadRequestMessage(body.message);
  }

  if (error.status === 403) {
    return 'Die Anfrage wurde aus Sicherheitsgründen abgelehnt. Bitte Seite neu laden und erneut versuchen.';
  }

  return 'Passwort konnte nicht geändert werden.';
}

function mapBadRequestMessage(message: string): string {
  if (message.includes('at least 8 characters')) {
    return 'Das neue Passwort muss mindestens 8 Zeichen lang sein.';
  }
  if (message.includes('differ from current password')) {
    return 'Das neue Passwort muss sich vom aktuellen Passwort unterscheiden.';
  }
  if (message.includes('New password is required')) {
    return 'Bitte geben Sie ein neues Passwort ein.';
  }
  if (message.includes('Current password is required')) {
    return 'Bitte geben Sie Ihr aktuelles Passwort ein.';
  }
  return 'Das neue Passwort ist ungültig.';
}
