import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { ColSortDTO, SortDirection } from '@dvb/util-angular';
import { SortIndicatorComponent } from './sort-indicator/sort-indicator.component';

const rotate: { [key: string]: SortDirection } = {
  ASC: 'DESC',
  DESC: 'NONE',
  NONE: 'ASC',
};

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'th[svuiSortable]',
    templateUrl: './sortable.component.html',
    styleUrl: './sortable.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [SortIndicatorComponent]
})
export class SortableComponent<T extends object> {
  readonly sortState = input<ColSortDTO<T> | undefined>();
  readonly sort = output<SortDirection>();

  direction = computed(() => this.sortState()?.sortDirection ?? 'NONE');

  rotate(): void {
    this.sort.emit(rotate[this.direction()]);
  }
}
