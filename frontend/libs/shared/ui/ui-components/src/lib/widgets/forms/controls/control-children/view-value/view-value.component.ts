import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Nullish } from '@dvb/util-shared';

@Component({
  selector: 'svui-view-value',
  standalone: true,
  templateUrl: './view-value.component.html',
  styleUrl: './view-value.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ViewValueComponent {
  readonly viewValue = input.required<string | Nullish>();
}
