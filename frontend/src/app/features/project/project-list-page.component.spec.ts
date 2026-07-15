import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ProjectListPageComponent } from './project-list-page.component';

describe('ProjectListPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectListPageComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should create without HttpClient provider (AD-10)', () => {
    const fixture = TestBed.createComponent(ProjectListPageComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render project list with detail links (Story 2.2)', () => {
    const fixture = TestBed.createComponent(ProjectListPageComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Projekte');
    expect(fixture.nativeElement.querySelector('a[href="/projects/demo-alpha"]')).toBeTruthy();
  });
});
