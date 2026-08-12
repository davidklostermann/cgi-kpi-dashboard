import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { vi } from 'vitest';

import { LoginPageComponent } from './login-page.component';
import { AuthService } from '../../core/auth/auth.service';

describe('LoginPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    vi.spyOn(TestBed.inject(AuthService), 'ensureCsrfCookie').mockResolvedValue(undefined);
  });

  it('should render login form without error banner initially', () => {
    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('h1')?.textContent).toContain('Anmelden');
    expect(element.querySelector('#login-username')).toBeTruthy();
    expect(element.querySelector('#login-password')).toBeTruthy();
    expect(element.querySelector('.login-alert')).toBeNull();
    expect(element.querySelector('.brand-title')?.textContent?.trim()).toBe('KPI Dashboard');
  });

  it('should show required-field validation without calling auth', async () => {
    const auth = TestBed.inject(AuthService);
    const loginSpy = vi.spyOn(auth, 'login');

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.detectChanges();

    await fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(loginSpy).not.toHaveBeenCalled();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('#login-username-error')?.textContent).toContain(
      'Benutzername ist erforderlich',
    );
    expect(element.querySelector('#login-password-error')?.textContent).toContain(
      'Passwort ist erforderlich',
    );
  });

  it('should call AuthService.login exactly once and disable submit while loading', async () => {
    const auth = TestBed.inject(AuthService);
    let resolveLogin!: (value: {
      userId: string;
      workspaceId: string;
      username: string;
      roles: string[];
      mustChangePassword: boolean;
    }) => void;
    const loginSpy = vi.spyOn(auth, 'login').mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveLogin = resolve;
        }),
    );

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'SecretPass1' });
    const submitPromise = fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(loginSpy).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.loading()).toBe(true);
    const button = (fixture.nativeElement as HTMLElement).querySelector(
      'button.login-submit',
    ) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
    expect(button.textContent).toContain('Anmeldung wird geprüft');

    resolveLogin({
      userId: 'u1',
      workspaceId: 'w1',
      username: 'admin',
      roles: ['ROLE_ADMIN'],
      mustChangePassword: false,
    });
    await submitPromise;
  });

  it('should navigate to portfolio after successful login', async () => {
    const auth = TestBed.inject(AuthService);
    const router = TestBed.inject(Router);
    vi.spyOn(auth, 'login').mockResolvedValue({
      userId: 'u1',
      workspaceId: 'w1',
      username: 'admin',
      roles: ['ROLE_ADMIN'],
      mustChangePassword: false,
    });
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'SecretPass1' });
    await fixture.componentInstance.submit();

    expect(navigateSpy).toHaveBeenCalledWith('/portfolio');
  });

  it('should map 401 without raw backend message', async () => {
    const auth = TestBed.inject(AuthService);
    vi.spyOn(auth, 'login').mockRejectedValue(
      new HttpErrorResponse({
        status: 401,
        error: { code: 'BAD_CREDENTIALS', message: 'Invalid username or password' },
      }),
    );

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'wrong' });
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    const alert = (fixture.nativeElement as HTMLElement).querySelector('.login-alert');
    expect(alert?.getAttribute('role')).toBe('alert');
    expect(alert?.textContent).toContain('Benutzername oder Passwort ist nicht korrekt.');
    expect(alert?.textContent).not.toContain('Invalid username or password');
  });

  it('should map 404 and 5xx without raw Resource not found', async () => {
    const auth = TestBed.inject(AuthService);
    const loginSpy = vi.spyOn(auth, 'login').mockRejectedValue(
      new HttpErrorResponse({
        status: 404,
        error: { code: 'NOT_FOUND', message: 'Resource not found' },
      }),
    );

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'SecretPass1' });
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    let alert = (fixture.nativeElement as HTMLElement).querySelector('.login-alert');
    expect(alert?.textContent).toContain('Die Anmeldung ist derzeit nicht verfügbar.');
    expect(alert?.textContent).not.toContain('Resource not found');

    loginSpy.mockRejectedValue(
      new HttpErrorResponse({
        status: 500,
        error: { code: 'INTERNAL_ERROR', message: 'An unexpected error occurred' },
      }),
    );
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    alert = (fixture.nativeElement as HTMLElement).querySelector('.login-alert');
    expect(alert?.textContent).toContain(
      'Die Anmeldung ist derzeit nicht möglich. Bitte versuchen Sie es später erneut.',
    );
    expect(alert?.textContent).not.toContain('An unexpected error occurred');
  });

  it('should map network failure without raw error text', async () => {
    const auth = TestBed.inject(AuthService);
    vi.spyOn(auth, 'login').mockRejectedValue(new HttpErrorResponse({ status: 0, statusText: 'Unknown Error' }));

    const fixture = TestBed.createComponent(LoginPageComponent);
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'SecretPass1' });
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    const alert = (fixture.nativeElement as HTMLElement).querySelector('.login-alert');
    expect(alert?.textContent).toContain('Der Anmeldedienst ist derzeit nicht erreichbar.');
  });
});
