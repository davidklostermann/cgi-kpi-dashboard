import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { AppShellComponent } from './app-shell.component';

describe('AppShellComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppShellComponent],
      providers: [provideRouter([]), provideAnimationsAsync()],
    }).compileComponents();
  });

  it('should render top navigation and side navigation (Story 2.1)', () => {
    const fixture = TestBed.createComponent(AppShellComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('app-top-nav')).toBeTruthy();
    expect(element.querySelector('app-side-nav')).toBeTruthy();
    expect(element.querySelector('mat-sidenav')).toBeTruthy();
  });

  it('should render the quiet CGI text brand in the top nav', () => {
    const fixture = TestBed.createComponent(AppShellComponent);
    fixture.detectChanges();

    const toolbar = fixture.nativeElement.querySelector('.cgi-top-nav') as HTMLElement;
    expect(toolbar).toBeTruthy();
    expect(toolbar.querySelector('.cgi-top-nav__brand-mark')?.textContent?.trim()).toBe('CGI');
    expect(toolbar.querySelector('.cgi-top-nav__brand-title')?.textContent?.trim()).toBe(
      'KPI Dashboard',
    );
    expect(toolbar.textContent).toContain('Portfolio & Projektsteuerung');
    expect(toolbar.querySelector('img.cgi-top-nav__logo')).toBeNull();
  });
});
