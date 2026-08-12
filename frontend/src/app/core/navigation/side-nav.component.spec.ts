import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { SideNavComponent } from './side-nav.component';
import { ActiveProjectNavService } from './active-project-nav.service';

describe('SideNavComponent', () => {
  let activeProjectNav: ActiveProjectNavService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideNavComponent],
      providers: [provideRouter([]), provideAnimationsAsync()],
    }).compileComponents();

    activeProjectNav = TestBed.inject(ActiveProjectNavService);
  });

  it('should contain Portfolio entry without a general Projekte link (Story 2.1)', () => {
    const fixture = TestBed.createComponent(SideNavComponent);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Portfolio');
    expect(text).not.toContain('Projekte');
  });

  it('should link Portfolio to /portfolio (Story 2.2)', () => {
    const fixture = TestBed.createComponent(SideNavComponent);
    fixture.detectChanges();
    const portfolioLink = fixture.nativeElement.querySelector(
      'a[href="/portfolio"]',
    ) as HTMLAnchorElement;

    expect(portfolioLink).toBeTruthy();
  });

  it('should show the active project under Projekt when one was opened', () => {
    activeProjectNav.setActiveProject(
      'a0000000-0000-4000-8000-000000000001',
      'Nexus Analytics Pilot',
    );

    const fixture = TestBed.createComponent(SideNavComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Nexus Analytics Pilot');
    const projectLink = fixture.nativeElement.querySelector(
      'a[href="/projects/a0000000-0000-4000-8000-000000000001"]',
    ) as HTMLAnchorElement;
    expect(projectLink).toBeTruthy();
  });

  it('should replace the active project entry when another project is opened', () => {
    activeProjectNav.setActiveProject('project-alpha', 'Projekt Alpha');

    const fixture = TestBed.createComponent(SideNavComponent);
    fixture.detectChanges();

    activeProjectNav.setActiveProject('project-beta', 'Projekt Beta');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Projekt Beta');
    expect(text).not.toContain('Projekt Alpha');
    expect(
      fixture.nativeElement.querySelector('a[href="/projects/project-beta"]'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('a[href="/projects/project-alpha"]'),
    ).toBeNull();
  });
});
