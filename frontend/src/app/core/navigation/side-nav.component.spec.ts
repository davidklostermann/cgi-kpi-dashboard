import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { SideNavComponent } from './side-nav.component';

describe('SideNavComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideNavComponent],
      providers: [provideRouter([]), provideAnimationsAsync()],
    }).compileComponents();
  });

  it('should contain Portfolio and Projekte entries (Story 2.1)', () => {
    const fixture = TestBed.createComponent(SideNavComponent);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Portfolio');
    expect(text).toContain('Projekte');
  });

  it('should link to /portfolio and /projects (Story 2.2)', () => {
    const fixture = TestBed.createComponent(SideNavComponent);
    fixture.detectChanges();
    const links = fixture.nativeElement.querySelectorAll('a[mat-list-item]');

    expect(links.length).toBe(2);
    expect(links[0].getAttribute('href')).toBe('/portfolio');
    expect(links[1].getAttribute('href')).toBe('/projects');
  });
});
