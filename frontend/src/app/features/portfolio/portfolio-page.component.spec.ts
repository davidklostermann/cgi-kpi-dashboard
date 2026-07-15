import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { PortfolioPageComponent } from './portfolio-page.component';

describe('PortfolioPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioPageComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should create without HttpClient provider (AD-10)', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render portfolio heading and facts-ai layout (Story 2.4)', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Portfolio');
    expect(fixture.nativeElement.querySelector('app-facts-ai-layout')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('KI-Einschätzung');
  });
});
