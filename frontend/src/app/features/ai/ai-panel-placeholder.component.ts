import { Component, input } from '@angular/core';

/** KI panel placeholder — presentation only, data via parent/services later (AD-7). */
@Component({
  selector: 'app-ai-panel-placeholder',
  template: `
    <div class="ki-panel" [attr.aria-label]="title()">
      <span class="ki-badge">KI-Einschätzung</span>
      <p class="ki-panel__message">{{ message() }}</p>
    </div>
  `,
  styles: `
    .ki-panel__message {
      margin: 0;
      line-height: 1.5;
    }

    .ki-badge {
      display: inline-block;
      background: var(--cgi-primary, #5236ab);
      color: #fff;
      font-size: 0.65rem;
      font-weight: 700;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      margin-bottom: 0.5rem;
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }
  `,
})
export class AiPanelPlaceholderComponent {
  readonly title = input('KI-Einschätzung');
  readonly message = input('KI-Inhalte folgen in Epic 8/9.');
}
