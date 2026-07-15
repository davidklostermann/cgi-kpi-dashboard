import { Routes } from '@angular/router';

export const PROJECT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./project-list-page.component').then((m) => m.ProjectListPageComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./project-detail-page.component').then((m) => m.ProjectDetailPageComponent),
  },
];
