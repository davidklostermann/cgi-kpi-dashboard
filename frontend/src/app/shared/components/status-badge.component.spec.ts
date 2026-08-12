import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusBadgeComponent } from './status-badge.component';

describe('StatusBadgeComponent', () => {
  let fixture: ComponentFixture<StatusBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusBadgeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StatusBadgeComponent);
    fixture.componentRef.setInput('status', 'ON_TRACK');
    fixture.componentRef.setInput('label', 'Auf Kurs');
    fixture.detectChanges();
  });

  it('should render dot and word label (Story 5.3)', () => {
    expect(fixture.nativeElement.textContent).toContain('Auf Kurs');
    expect(fixture.nativeElement.querySelector('.status-badge__dot')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.status-badge--on-track')).toBeTruthy();
  });
});
