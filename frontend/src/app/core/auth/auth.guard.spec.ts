import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  it('redirects unauthenticated users to login', async () => {
    await TestBed.configureTestingModule({
      providers: [provideRouter([])],
    }).compileComponents();

    const auth = TestBed.inject(AuthService);
    vi.spyOn(auth, 'sessionInitialized').mockReturnValue(true);

    const router = TestBed.inject(Router);
    const result = await TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/portfolio' } as never),
    );

    expect(result).toEqual(
      router.createUrlTree(['/login'], { queryParams: { returnUrl: '/portfolio' } }),
    );
  });
});
