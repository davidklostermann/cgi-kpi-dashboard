import { Component, input } from '@angular/core';

/** Two-column layout: facts main area + KI sidebar (AD-7, FR-10). */
@Component({
  selector: 'app-facts-ai-layout',
  templateUrl: './facts-ai-layout.component.html',
  styleUrl: './facts-ai-layout.component.scss',
})
export class FactsAiLayoutComponent {
  readonly hasAiContent = input<boolean>(false);
}
