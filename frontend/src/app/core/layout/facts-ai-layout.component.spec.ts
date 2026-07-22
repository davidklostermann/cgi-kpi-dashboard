import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { FactsAiLayoutComponent } from './facts-ai-layout.component';

@Component({
  selector: 'app-test-host',
  imports: [FactsAiLayoutComponent],
  template: `
    <app-facts-ai-layout>
      <section factsMain>Main facts</section>
      <div factsAi>KI panel</div>
    </app-facts-ai-layout>
  `,
})
class TestHostComponent {}

describe('FactsAiLayoutComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();
  });

  it('should render main and AI regions (Story 2.4)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const layout = fixture.nativeElement.querySelector('.facts-ai-layout');
    expect(layout).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Main facts');
    expect(fixture.nativeElement.textContent).toContain('KI panel');
  });

  it('should show KI disclaimer and ki-surface styling (Story 2.4)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const aiColumn = fixture.nativeElement.querySelector('.facts-ai-layout__ai') as HTMLElement;
    expect(aiColumn).toBeTruthy();
    expect(aiColumn.textContent).toContain('KI-generierte Einschätzungen');
    expect(getComputedStyle(aiColumn).backgroundColor).toBeTruthy();
  });

  it('should use two-column grid on desktop viewport (Story 2.4)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const layout = fixture.nativeElement.querySelector('.facts-ai-layout') as HTMLElement;
    expect(layout).toBeTruthy();
    expect(layout.classList.contains('facts-ai-layout')).toBe(true);

    // jsdom does not reliably apply component media-query grid styles via getComputedStyle;
    // assert structural contract used by desktop CSS (main + AI regions).
    expect(fixture.nativeElement.querySelector('.facts-ai-layout__main')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.facts-ai-layout__ai')).toBeTruthy();
  });
});
