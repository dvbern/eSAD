import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  standalone: true,
  templateUrl: './typeahead-no-results.component.html',
  styleUrl: './typeahead-no-results.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TypeaheadNoResultsComponent {
  readonly noResults = input<boolean>(false);
  readonly noResultsMessage = input.required<string>();
}
