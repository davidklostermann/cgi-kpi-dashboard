import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

import { AuthService } from './auth.service';
import { AuthApiService } from '../api/auth-api.service';
import { PortfolioFilterService } from '../../features/portfolio/portfolio-filter.service';

describe('AuthService', () => {
  let service: AuthService;
  let authApi: AuthApiService;
  let filters: PortfolioFilterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    authApi = TestBed.inject(AuthApiService);
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

  it('logout clears portfolio filters', async () => {
    filters.update({ customer: 'Acme' });
    vi.spyOn(authApi, 'logout').mockReturnValue(of(void 0));

    await service.logout();

    expect(filters.hasActiveFilters()).toBe(false);
    expect(service.isAuthenticated()).toBe(false);
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
});
