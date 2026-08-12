import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { csrfInterceptor } from './csrf.interceptor';

describe('csrfInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token; path=/';

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([csrfInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('should attach X-XSRF-TOKEN header on POST requests', async () => {
    const responsePromise = firstValueFrom(http.post('/api/auth/login', {}));

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
    req.flush({});

    await responsePromise;
  });

  it('should not attach CSRF header on GET requests', async () => {
    const responsePromise = firstValueFrom(http.get('/api/auth/me'));

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.headers.has('X-XSRF-TOKEN')).toBe(false);
    req.flush({});

    await responsePromise;
  });
});
