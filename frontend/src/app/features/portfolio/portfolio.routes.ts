import { Routes } from '@angular/router';

export const PORTFOLIO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./portfolio-page.component').then((m) => m.PortfolioPageComponent),
  },
];
