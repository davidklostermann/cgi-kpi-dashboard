import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';
import { mustChangePasswordGuard } from './core/auth/must-change-password.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'change-password',
    loadComponent: () =>
      import('./features/auth/change-password-page.component').then(
        (m) => m.ChangePasswordPageComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/app-shell.component').then((m) => m.AppShellComponent),
    canActivate: [authGuard, mustChangePasswordGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'portfolio' },
      {
        path: 'portfolio',
        loadChildren: () =>
          import('./features/portfolio/portfolio.routes').then((m) => m.PORTFOLIO_ROUTES),
      },
      {
        path: 'projects',
        loadChildren: () =>
          import('./features/project/project.routes').then((m) => m.PROJECT_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: 'portfolio' },
];
