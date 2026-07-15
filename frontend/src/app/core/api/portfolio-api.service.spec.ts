import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';

import { PortfolioApiService } from './portfolio-api.service';

describe('PortfolioApiService', () => {
  let service: PortfolioApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(PortfolioApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getHealthProbe should call /actuator/health without /api prefix', async () => {
    const responsePromise = firstValueFrom(service.getHealthProbe());

    const req = httpMock.expectOne('/actuator/health');
    expect(req.request.method).toBe('GET');
    req.flush({ status: 'UP' });

    await expect(responsePromise).resolves.toEqual({ status: 'UP' });
  });
});
