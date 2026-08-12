import { Component, input } from '@angular/core';

/** Status indicator with dot + word label (UX-DR4). */
@Component({
  selector: 'app-status-badge',
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss',
})
export class StatusBadgeComponent {
  readonly status = input.required<string>();
  readonly label = input.required<string>();
}
