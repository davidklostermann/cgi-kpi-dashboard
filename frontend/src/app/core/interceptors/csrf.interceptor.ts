import { HttpInterceptorFn } from '@angular/common/http';

const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';

function readCookie(name: string): string | null {
  if (typeof document === 'undefined') {
    return null;
  }
  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = document.cookie.match(new RegExp(`(?:^|; )${escaped}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

/** Attach Spring CSRF header for state-changing API requests (AD-12). */
export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  if (!['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method)) {
    return next(req);
  }

  const token = readCookie(CSRF_COOKIE);
  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { [CSRF_HEADER]: token },
    }),
  );
};
