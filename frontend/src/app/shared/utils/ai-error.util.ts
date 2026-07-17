import { HttpErrorResponse } from '@angular/common/http';

export type AiPanelStatus = 'error' | 'disabled';

export interface AiPanelErrorView {
  status: AiPanelStatus;
  message: string;
  code: string | null;
}

interface ApiErrorBody {
  code?: string;
  message?: string;
}

export function resolveAiPanelError(error: unknown, fallback: string): AiPanelErrorView {
  if (!(error instanceof HttpErrorResponse)) {
    return { status: 'error', message: fallback, code: null };
  }

  const body = (error.error ?? null) as ApiErrorBody | null;
  const code = body?.code?.trim() || null;
  const message = body?.message?.trim() || fallback;

  if (code === 'AI_DISABLED') {
    return { status: 'disabled', message, code };
  }

  if (code === 'AI_PROVIDER_ERROR' || code === 'AI_UNAVAILABLE') {
    return { status: 'error', message: withDiagnosticCode(message, code), code };
  }

  return { status: 'error', message: withDiagnosticCode(message, code), code };
}

function withDiagnosticCode(message: string, code: string | null): string {
  if (!code || message.includes(`(${code})`)) {
    return message;
  }
  return `${message} (${code})`;
}
