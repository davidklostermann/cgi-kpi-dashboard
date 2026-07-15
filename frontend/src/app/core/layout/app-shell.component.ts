import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';

import { SideNavComponent } from '../navigation/side-nav.component';
import { TopNavComponent } from '../navigation/top-nav.component';

/** CGI application shell — Material stub until EDS integration (AD-11). */
@Component({
  selector: 'app-shell',
  imports: [MatSidenavModule, RouterOutlet, TopNavComponent, SideNavComponent],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {}
