import { Routes } from '@angular/router';
import { adminGuard } from '../../core/auth/admin.guard';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'users' },
      {
        path: 'users',
        loadComponent: () => import('./user-management/user-management.component').then(m => m.UserManagementComponent),
      }
    ],
    canActivate: [adminGuard]
  }
];
