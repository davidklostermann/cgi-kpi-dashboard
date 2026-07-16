import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { PortfolioFilterService } from './portfolio-filter.service';
import { EMPTY_PORTFOLIO_FILTERS } from '../../shared/models/portfolio-filter.model';

@Component({ template: '' })
class StubPageComponent {}

describe('PortfolioFilterService', () => {
  let service: PortfolioFilterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'portfolio', component: StubPageComponent },
          { path: 'projects/:id', component: StubPageComponent },
        ]),
      ],
    });
    service = TestBed.inject(PortfolioFilterService);
    service.reset();
  });

  it('should expose default filters (Story 4.4)', () => {
    expect(service.filters()).toEqual(EMPTY_PORTFOLIO_FILTERS);
  });

  it('should map filters to API query params (Story 4.4)', () => {
    service.update({
      customer: 'Acme GmbH',
      statuses: ['CRITICAL'],
      lifecycle: 'active',
    });

    expect(service.toQueryParams()).toEqual({
      customer: 'Acme GmbH',
      status: ['CRITICAL'],
      lifecycle: 'active',
    });
  });

  it('should preserve filter state across navigation (Story 5.5)', async () => {
    service.update({ customer: 'Acme GmbH', lifecycle: 'active' });

    const router = TestBed.inject(Router);
    await router.navigate(['/projects', 'a0000000-0000-4000-8000-000000000001']);
    await router.navigate(['/portfolio']);

    expect(service.filters().customer).toBe('Acme GmbH');
    expect(service.filters().lifecycle).toBe('active');
  });
});
