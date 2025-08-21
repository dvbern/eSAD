import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'td[svuiRowClickableCell]',
  standalone: true,
  templateUrl: './row-clickable-cell.component.html',
  styleUrl: './row-clickable-cell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RowClickableCellComponent {
  preventRowClick($event: MouseEvent): void {
    $event.preventDefault();
    $event.stopPropagation();
  }
}
