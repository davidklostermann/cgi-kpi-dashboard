import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import {
  AuthUser,
  ChangePasswordRequest,
  LoginRequest,
} from '../../shared/models/auth.model';

/** Auth endpoints (Story 11.4 backend). */
@Injectable({ providedIn: 'root' })
export class AuthApiService extends ApiClient {
  login(body: LoginRequest) {
    return this.post<AuthUser>('/auth/login', body);
  }

  logout() {
    return this.post<void>('/auth/logout', {});
  }

  me() {
    return this.get<AuthUser>('/auth/me');
  }

  changePassword(body: ChangePasswordRequest) {
    return this.post<void>('/auth/change-password', body);
  }

  /** Public bootstrap call that materializes Spring's CSRF cookie before login. */
  bootstrapCsrf() {
    return this.get<{ status: string }>('/auth/csrf');
  }
}
