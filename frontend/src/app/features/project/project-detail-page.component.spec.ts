import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ProjectDetailPageComponent } from './project-detail-page.component';

describe('ProjectDetailPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectDetailPageComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should show breadcrumb Portfolio > Projektname (Story 2.3)', () => {
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'demo-alpha');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Portfolio');
    expect(text).toContain('Projekt Alpha');
  });
});
