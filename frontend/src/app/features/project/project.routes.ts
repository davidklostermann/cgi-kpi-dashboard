import { Routes } from '@angular/router';

export const PROJECT_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: '/portfolio',
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./project-detail-page.component').then((m) => m.ProjectDetailPageComponent),
  },
];
