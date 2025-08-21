import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { ControlWithIdWrapperDirective } from '../control-with-id-wrapper.directive';

@Component({
    selector: 'svui-table-filter',
    templateUrl: './table-filter.component.html',
    styleUrl: './table-filter.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [TranslocoPipe]
})
export class TableFilterComponent extends ControlWithIdWrapperDirective {
  readonly columnHeader = input.required<string>();
}
