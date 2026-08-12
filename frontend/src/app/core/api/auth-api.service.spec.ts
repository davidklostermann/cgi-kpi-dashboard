import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';

import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('login should POST /api/auth/login', async () => {
    const responsePromise = firstValueFrom(
      service.login({ username: 'admin', password: 'SecretPass1' }),
    );

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      userId: 'u1',
      workspaceId: 'w1',
      username: 'admin',
      roles: ['ROLE_ADMIN'],
      mustChangePassword: false,
    });

    await expect(responsePromise).resolves.toMatchObject({ username: 'admin' });
  });

  it('me should GET /api/auth/me', async () => {
    const responsePromise = firstValueFrom(service.me());

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    req.flush({
      userId: 'u1',
      workspaceId: 'w1',
      username: 'admin',
      roles: ['ROLE_ADMIN'],
      mustChangePassword: false,
    });

    await expect(responsePromise).resolves.toMatchObject({ username: 'admin' });
  });

  it('logout should POST /api/auth/logout', async () => {
    const responsePromise = firstValueFrom(service.logout());

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush(null);

    await expect(responsePromise).resolves.toBeNull();
  });
});
