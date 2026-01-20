import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, HostBinding, input} from '@angular/core';
import {InfoItemComponent} from '../info-items/info-item/info-item.component';

@Component({
  selector: 'svui-summary-values',
  templateUrl: './summary-values.component.html',
  styleUrl: './summary-values.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [DatePipe],
  imports: [InfoItemComponent],
  host: {
    '[class.display-grid]' : 'itemFlow() === "grid"',
    '[class.display-flex]' : 'itemFlow() === "flex"',
  }
})
export class SummaryValuesComponent {
  readonly map = input.required<Map<string, number>>();

  readonly itemFlow = input<'flex' | 'grid'>('grid');
}
