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
    const raw = error instanceof Error ? error.message : String(error ?? '');
    if (/failed to fetch/i.test(raw)) {
      return { status: 'error', message: fallback, code: null };
    }
    return { status: 'error', message: fallback, code: null };
  }

  const body = (error.error ?? null) as ApiErrorBody | null;
  const code = body?.code?.trim() || null;
  let message = body?.message?.trim() || fallback;
  if (/failed to fetch/i.test(message) || error.status === 0) {
    message = fallback;
  }

  if (code === 'AI_DISABLED') {
    return { status: 'disabled', message: body?.message?.trim() || message, code };
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
