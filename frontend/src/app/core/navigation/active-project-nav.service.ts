import { Injectable, signal } from '@angular/core';

export interface ActiveProjectNavItem {
  id: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class ActiveProjectNavService {
  private readonly activeProjectState = signal<ActiveProjectNavItem | null>(null);

  readonly activeProject = this.activeProjectState.asReadonly();

  setActiveProject(id: string, name: string): void {
    this.activeProjectState.set({ id, name });
  }

  clear(): void {
    this.activeProjectState.set(null);
  }
}
