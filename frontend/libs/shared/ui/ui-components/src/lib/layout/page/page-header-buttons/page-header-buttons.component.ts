import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {ButtonComponent, IconOnlyButtonComponent} from '../../../widgets';

@Component({
  selector: 'svui-page-header-buttons',
  templateUrl: './page-header-buttons.component.html',
  styleUrl: './page-header-buttons.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IconOnlyButtonComponent, TranslocoPipe, ButtonComponent, ButtonComponent]
})
export class PageHeaderButtonsComponent {
  showFilterPopup = input<boolean>(false);
  filter = output<void>();
  activeFilterCount = input<number>();
  activeErweiterteFilterCount = input<number | null>(null);
  resetFilter = output<void>();

  protected readonly alert = alert;
}
