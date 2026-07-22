import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';

import { ChangePasswordPageComponent } from './change-password-page.component';
import { AuthService } from '../../core/auth/auth.service';

describe('ChangePasswordPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangePasswordPageComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    vi.spyOn(TestBed.inject(AuthService), 'ensureCsrfCookie').mockResolvedValue(undefined);
  });

  it('should show min-length validation error from backend', async () => {
    const auth = TestBed.inject(AuthService);
    vi.spyOn(auth, 'changePassword').mockRejectedValue(
      new HttpErrorResponse({
        status: 400,
        error: { code: 'BAD_REQUEST', message: 'New password must be at least 8 characters' },
      }),
    );

    const fixture = TestBed.createComponent(ChangePasswordPageComponent);
    fixture.componentInstance.form.setValue({
      currentPassword: 'BootstrapInit1',
      newPassword: 'ValidPass9',
      confirmPassword: 'ValidPass9',
    });
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    const alert = (fixture.nativeElement as HTMLElement).querySelector('.auth-card__error');
    expect(alert?.textContent).toContain('mindestens 8 Zeichen');
  });
});
