import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { SortDirection } from '@dvb/util-angular';
import { IconComponent } from '../../../icons/icon';

@Component({
    selector: 'svui-sort-indicator',
    templateUrl: './sort-indicator.component.html',
    styleUrl: './sort-indicator.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [IconComponent]
})
export class SortIndicatorComponent {
  readonly sortDirection = input.required<SortDirection>();
}
