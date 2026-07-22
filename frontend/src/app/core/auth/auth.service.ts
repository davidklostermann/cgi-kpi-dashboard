import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AuthApiService } from '../api/auth-api.service';
import { PortfolioFilterService } from '../../features/portfolio/portfolio-filter.service';
import {
  AuthUser,
  ChangePasswordRequest,
  LoginRequest,
} from '../../shared/models/auth.model';

/** Session state — in-memory only, no localStorage (AD-12). */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly portfolioFilterService = inject(PortfolioFilterService);

  private readonly user = signal<AuthUser | null>(null);
  private readonly sessionReady = signal(false);

  readonly currentUser = this.user.asReadonly();
  readonly isAuthenticated = computed(() => this.user() !== null);
  readonly isAdmin = computed(() => this.user()?.roles.includes('ROLE_ADMIN') ?? false);
  readonly mustChangePassword = computed(() => this.user()?.mustChangePassword ?? false);

  sessionInitialized(): boolean {
    return this.sessionReady();
  }

  async initializeSession(): Promise<void> {
    try {
      const me = await firstValueFrom(this.authApi.me());
      this.user.set(me);
    } catch {
      this.user.set(null);
    } finally {
      this.sessionReady.set(true);
    }
  }

  ensureCsrfCookie(): Promise<void> {
    return firstValueFrom(this.authApi.bootstrapCsrf()).then(() => undefined);
  }

  async login(request: LoginRequest): Promise<AuthUser> {
    const user = await firstValueFrom(this.authApi.login(request));
    this.user.set(user);
    this.sessionReady.set(true);
    return user;
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(this.authApi.logout());
    } finally {
      this.clearClientState();
    }
  }

  async changePassword(request: ChangePasswordRequest): Promise<void> {
    await firstValueFrom(this.authApi.changePassword(request));
    const me = await firstValueFrom(this.authApi.me());
    this.user.set(me);
  }

  clearClientState(): void {
    this.user.set(null);
    this.sessionReady.set(true);
    this.portfolioFilterService.reset();
  }

  primaryRoleLabel(): string | null {
    const roles = this.user()?.roles ?? [];
    const primary = roles[0]?.replace(/^ROLE_/, '') ?? '';
    if (primary === 'ADMIN') {
      return 'Administrator';
    }
    if (primary === 'USER') {
      return 'Benutzer';
    }
    return primary || null;
  }
}
