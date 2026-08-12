import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { KpiCardComponent } from './kpi-card.component';

@Component({
  selector: 'app-test-host',
  imports: [KpiCardComponent],
  template: `
    <app-kpi-card
      label="Aktive Projekte"
      [value]="19"
      unit="Projekte"
      delta="−1 zur Vorperiode"
    />
  `,
})
class TestHostComponent {}

describe('KpiCardComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();
  });

  it('should render label, value and unit from inputs (Story 4.2)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const card = fixture.nativeElement.querySelector('.kpi-card') as HTMLElement;
    expect(card).toBeTruthy();
    expect(card.textContent).toContain('Aktive Projekte');
    expect(card.textContent).toContain('19');
    expect(card.textContent).toContain('Projekte');
    expect(card.textContent).toContain('−1 zur Vorperiode');
  });

  it('should apply tabular-nums to value and unit (Story 4.2)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const number = fixture.nativeElement.querySelector('.kpi-card__number') as HTMLElement;
    const unit = fixture.nativeElement.querySelector('.kpi-card__unit') as HTMLElement;

    expect(getComputedStyle(number).fontVariantNumeric).toBe('tabular-nums');
    expect(getComputedStyle(unit).fontVariantNumeric).toBe('tabular-nums');
  });

  it('should be read-only presentation without interactive controls (Story 4.2)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const card = fixture.nativeElement.querySelector('.kpi-card') as HTMLElement;
    expect(card.querySelector('button')).toBeNull();
    expect(card.querySelector('a')).toBeNull();
    expect(card.querySelector('input')).toBeNull();
    expect(card.tagName.toLowerCase()).toBe('article');
  });

  it('should use CGI surface and border tokens (Story 4.2)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const card = fixture.nativeElement.querySelector('.kpi-card') as HTMLElement;
    const styles = getComputedStyle(card);

    expect(styles.backgroundColor).toBeTruthy();
    expect(styles.borderWidth).not.toBe('0px');
    expect(styles.borderRadius).not.toBe('0px');
  });
});
