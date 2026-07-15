import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/app-shell.component').then((m) => m.AppShellComponent),
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
