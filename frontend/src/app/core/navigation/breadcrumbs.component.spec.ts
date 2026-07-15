import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { BreadcrumbsComponent } from './breadcrumbs.component';

@Component({ template: '' })
class PortfolioStubComponent {}

describe('BreadcrumbsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BreadcrumbsComponent],
      providers: [
        provideRouter([
          { path: 'portfolio', component: PortfolioStubComponent },
          { path: 'projects/:id', component: PortfolioStubComponent },
        ]),
      ],
    }).compileComponents();
  });

  it('should render Portfolio > Projektname trail (Story 2.3)', () => {
    const fixture = TestBed.createComponent(BreadcrumbsComponent);
    fixture.componentRef.setInput('items', [
      { label: 'Portfolio', route: ['/portfolio'] },
      { label: 'Projekt Alpha' },
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Portfolio');
    expect(fixture.nativeElement.textContent).toContain('Projekt Alpha');
  });

  it('should navigate to /portfolio when Portfolio crumb is clicked (Story 2.3)', async () => {
    const fixture = TestBed.createComponent(BreadcrumbsComponent);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/projects/demo-alpha');

    fixture.componentRef.setInput('items', [
      { label: 'Portfolio', route: ['/portfolio'] },
      { label: 'Projekt Alpha' },
    ]);
    fixture.detectChanges();

    const link = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;
    link.click();
    await fixture.whenStable();

    expect(router.url).toBe('/portfolio');
  });
});
