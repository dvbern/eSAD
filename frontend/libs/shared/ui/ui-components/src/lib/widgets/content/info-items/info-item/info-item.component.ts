import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { WithIdDirective } from '../../../forms/inputs/with-id.directive';

@Component({
  selector: 'svui-info-item',
  standalone: true,
  templateUrl: './info-item.component.html',
  styleUrl: './info-item.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InfoItemComponent extends WithIdDirective {
  readonly label = input<string>();
  readonly count = input<string | null>(null);
}
