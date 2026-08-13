import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

import { AuthService } from './auth.service';
import { AuthApiService } from '../api/auth-api.service';
import { ActiveProjectNavService } from '../navigation/active-project-nav.service';
import { PortfolioFilterService } from '../../features/portfolio/portfolio-filter.service';

describe('AuthService', () => {
  let service: AuthService;
  let authApi: AuthApiService;
  let activeProjectNav: ActiveProjectNavService;
  let filters: PortfolioFilterService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    authApi = TestBed.inject(AuthApiService);
    activeProjectNav = TestBed.inject(ActiveProjectNavService);
    filters = TestBed.inject(PortfolioFilterService);
  });

  it('initializeSession sets user on successful me()', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w1',
        username: 'admin',
        roles: ['ROLE_ADMIN'],
        mustChangePassword: false,
      }),
    );

    await service.initializeSession();

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.username).toBe('admin');
  });

  it('initializeSession clears user on me() failure', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(throwError(() => new Error('401')));

    await service.initializeSession();

    expect(service.isAuthenticated()).toBe(false);
  });

  it('logout clears all account-scoped navigation and filter state', async () => {
    filters.update({ customer: 'Acme' });
    activeProjectNav.setActiveProject('project-a', 'Projekt A');
    vi.spyOn(authApi, 'logout').mockReturnValue(of(void 0));

    await service.logout();

    expect(filters.hasActiveFilters()).toBe(false);
    expect(activeProjectNav.activeProject()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('clears account-scoped state and resolves even when logout fails', async () => {
    filters.update({ customer: 'Acme' });
    activeProjectNav.setActiveProject('project-a', 'Projekt A');
    vi.spyOn(authApi, 'logout').mockReturnValue(throwError(() => new Error('network')));

    await expect(service.logout()).resolves.toBeUndefined();

    expect(filters.hasActiveFilters()).toBe(false);
    expect(activeProjectNav.activeProject()).toBeNull();
  });

  it('does not restore the previous route after a different user logs in', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w1',
        username: 'first',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );
    await service.initializeSession();
    activeProjectNav.setActiveProject('project-a', 'Projekt A');
    service.clearClientState();

    vi.spyOn(authApi, 'login').mockReturnValue(
      of({
        userId: 'u2',
        workspaceId: 'w1',
        username: 'second',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );
    await service.login({ username: 'second', password: 'SecretPass1' });

    expect(service.canRestorePreviousRoute()).toBe(false);
    expect(activeProjectNav.activeProject()).toBeNull();
  });

  it('allows route restoration when the same user reauthenticates', async () => {
    const user = {
      userId: 'u1',
      workspaceId: 'w1',
      username: 'first',
      roles: ['ROLE_USER'],
      mustChangePassword: false,
    };
    vi.spyOn(authApi, 'me').mockReturnValue(of(user));
    await service.initializeSession();
    service.clearClientState();
    vi.spyOn(authApi, 'login').mockReturnValue(of(user));

    await service.login({ username: 'first', password: 'SecretPass1' });

    expect(service.canRestorePreviousRoute()).toBe(true);
  });

  it('treats the same user in another workspace as a different identity', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w1',
        username: 'first',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );
    await service.initializeSession();
    service.clearClientState();
    vi.spyOn(authApi, 'login').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w2',
        username: 'first',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );

    await service.login({ username: 'first', password: 'SecretPass1' });

    expect(service.canRestorePreviousRoute()).toBe(false);
  });

  it('retains the previous identity across a login-page reload', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w1',
        username: 'first',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );
    await service.initializeSession();
    service.clearClientState();

    const reloadedService = TestBed.runInInjectionContext(() => new AuthService());
    vi.spyOn(authApi, 'login').mockReturnValue(
      of({
        userId: 'u2',
        workspaceId: 'w1',
        username: 'second',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );

    await reloadedService.login({ username: 'second', password: 'SecretPass1' });

    expect(reloadedService.canRestorePreviousRoute()).toBe(false);
  });

  it('primaryRoleLabel maps ADMIN to Administrator', () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w1',
        username: 'admin',
        roles: ['ROLE_ADMIN'],
        mustChangePassword: false,
      }),
    );

    return service.initializeSession().then(() => {
      expect(service.primaryRoleLabel()).toBe('Administrator');
    });
  });

  it('isAdmin is true only for ROLE_ADMIN after session init', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u1',
        workspaceId: 'w1',
        username: 'admin',
        roles: ['ROLE_ADMIN'],
        mustChangePassword: false,
      }),
    );

    await service.initializeSession();

    expect(service.isAdmin()).toBe(true);
  });

  it('isAdmin is false for ROLE_USER', async () => {
    vi.spyOn(authApi, 'me').mockReturnValue(
      of({
        userId: 'u2',
        workspaceId: 'w1',
        username: 'user',
        roles: ['ROLE_USER'],
        mustChangePassword: false,
      }),
    );

    await service.initializeSession();

    expect(service.isAdmin()).toBe(false);
  });

  it('isAdmin is false before session init and after me() failure', async () => {
    expect(service.isAdmin()).toBe(false);

    vi.spyOn(authApi, 'me').mockReturnValue(throwError(() => new Error('401')));

    await service.initializeSession();

    expect(service.isAdmin()).toBe(false);
    expect(service.isAuthenticated()).toBe(false);
  });
});
