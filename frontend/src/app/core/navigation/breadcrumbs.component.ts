import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BreadcrumbItem } from './breadcrumb.model';

@Component({
  selector: 'app-breadcrumbs',
  imports: [RouterLink],
  templateUrl: './breadcrumbs.component.html',
  styleUrl: './breadcrumbs.component.scss',
})
export class BreadcrumbsComponent {
  readonly items = input.required<BreadcrumbItem[]>();
}
