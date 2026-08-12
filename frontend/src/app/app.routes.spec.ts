import { Type } from '@angular/core';
import { Routes } from '@angular/router';

import { routes } from './app.routes';
import { AppShellComponent } from './core/layout/app-shell.component';
import { ProjectDetailPageComponent } from './features/project/project-detail-page.component';

describe('App routes (Story 2.2 / 11.5)', () => {
  it('should expose login and change-password routes outside shell', async () => {
    const loginRoute = routes.find((route) => route.path === 'login');
    const changePasswordRoute = routes.find((route) => route.path === 'change-password');

    expect(loginRoute?.loadComponent).toBeDefined();
    expect(changePasswordRoute?.loadComponent).toBeDefined();
    expect(changePasswordRoute?.canActivate).toBeDefined();
  });

  it('should load shell with portfolio and projects child routes', async () => {
    const shellRoute = routes.find((route) => route.path === '');
    expect(shellRoute?.loadComponent).toBeDefined();
    expect(shellRoute?.canActivate?.length).toBeGreaterThan(0);

    const shellModule = (await shellRoute!.loadComponent!()) as Type<AppShellComponent>;
    expect(shellModule).toBe(AppShellComponent);

    const portfolioRoute = shellRoute!.children!.find((child) => child.path === 'portfolio');
    const projectRoute = shellRoute!.children!.find((child) => child.path === 'projects');

    expect(portfolioRoute?.loadChildren).toBeDefined();
    expect(projectRoute?.loadChildren).toBeDefined();

    const portfolioRoutes = (await (portfolioRoute!.loadChildren as () => Promise<Routes>)()) as Routes;
    const projectRoutes = (await (projectRoute!.loadChildren as () => Promise<Routes>)()) as Routes;

    expect(portfolioRoutes.length).toBeGreaterThan(0);
    expect(projectRoutes.length).toBeGreaterThan(0);
    expect(portfolioRoutes[0].loadComponent).toBeDefined();
    expect(projectRoutes.some((route) => route.path === ':id')).toBe(true);
  });

  it('should lazy load project detail route for :id', async () => {
    const shellRoute = routes.find((route) => route.path === '');
    const projectRoute = shellRoute!.children!.find((child) => child.path === 'projects');
    const projectRoutes = (await (projectRoute!.loadChildren as () => Promise<Routes>)()) as Routes;

    const detailRoute = projectRoutes.find((route) => route.path === ':id');
    expect(detailRoute?.loadComponent).toBeDefined();

    const detailModule = (await detailRoute!.loadComponent!()) as Type<ProjectDetailPageComponent>;
    expect(detailModule).toBe(ProjectDetailPageComponent);
  });
});
