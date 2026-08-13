import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AuthApiService } from '../api/auth-api.service';
import { ActiveProjectNavService } from '../navigation/active-project-nav.service';
import { PortfolioFilterService } from '../../features/portfolio/portfolio-filter.service';
import {
  AuthUser,
  ChangePasswordRequest,
  LoginRequest,
} from '../../shared/models/auth.model';

const RETURN_ROUTE_OWNER_KEY = 'cgi-kpi.return-route-owner';

/** Session state — in-memory only, no localStorage (AD-12). */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly activeProjectNav = inject(ActiveProjectNavService);
  private readonly portfolioFilterService = inject(PortfolioFilterService);

  private readonly user = signal<AuthUser | null>(null);
  private readonly sessionReady = signal(false);
  private lastSessionIdentity: string | null = this.readReturnRouteOwner();
  private restorePreviousRouteAfterLogin = true;

  readonly currentUser = this.user.asReadonly();
  readonly isAuthenticated = computed(() => this.user() !== null);
  readonly isAdmin = computed(() => {
    if (!this.sessionReady()) {
      return false;
    }
    const roles = this.user()?.roles;
    if (!roles?.length) {
      return false;
    }
    return roles.includes('ROLE_ADMIN');
  });
  readonly mustChangePassword = computed(() => this.user()?.mustChangePassword ?? false);

  sessionInitialized(): boolean {
    return this.sessionReady();
  }

  async initializeSession(): Promise<void> {
    try {
      const me = await firstValueFrom(this.authApi.me());
      this.applyAuthenticatedUser(me);
    } catch {
      this.clearClientState();
    } finally {
      this.sessionReady.set(true);
    }
  }

  ensureCsrfCookie(): Promise<void> {
    return firstValueFrom(this.authApi.bootstrapCsrf()).then(() => undefined);
  }

  async login(request: LoginRequest): Promise<AuthUser> {
    const user = await firstValueFrom(this.authApi.login(request));
    this.applyAuthenticatedUser(user);
    this.sessionReady.set(true);
    return user;
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(this.authApi.logout());
    } catch {
      // Client-side isolation must not depend on the logout request succeeding.
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
    const currentIdentity = this.identityOf(this.user());
    if (currentIdentity) {
      this.lastSessionIdentity = currentIdentity;
      this.writeReturnRouteOwner(currentIdentity);
    }
    this.user.set(null);
    this.sessionReady.set(true);
    this.activeProjectNav.clear();
    this.portfolioFilterService.reset();
  }

  canRestorePreviousRoute(): boolean {
    return this.restorePreviousRouteAfterLogin;
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

  private applyAuthenticatedUser(nextUser: AuthUser): void {
    const nextIdentity = this.identityOf(nextUser)!;
    const previousIdentity = this.identityOf(this.user()) ?? this.lastSessionIdentity;
    const identityChanged = previousIdentity !== null && previousIdentity !== nextIdentity;

    this.restorePreviousRouteAfterLogin = !identityChanged;
    if (identityChanged) {
      this.activeProjectNav.clear();
      this.portfolioFilterService.reset();
    }

    this.lastSessionIdentity = nextIdentity;
    this.writeReturnRouteOwner(nextIdentity);
    this.user.set(nextUser);
  }

  private identityOf(user: AuthUser | null): string | null {
    return user ? `${user.workspaceId}:${user.userId}` : null;
  }

  private readReturnRouteOwner(): string | null {
    try {
      return globalThis.sessionStorage?.getItem(RETURN_ROUTE_OWNER_KEY) ?? null;
    } catch {
      return null;
    }
  }

  private writeReturnRouteOwner(identity: string): void {
    try {
      globalThis.sessionStorage?.setItem(RETURN_ROUTE_OWNER_KEY, identity);
    } catch {
      // Session storage can be unavailable in hardened browser configurations.
    }
  }
}
