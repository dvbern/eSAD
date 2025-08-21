import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {NgbTooltip} from '@ng-bootstrap/ng-bootstrap';
import {IconComponent, IconUseCase} from '../icon';

type IconColor = 'default' | 'success' | 'danger';

@Component({
  selector: 'svui-icon-with-tooltip',
  templateUrl: './icon-with-tooltip.component.html',
  styleUrl: './icon-with-tooltip.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IconComponent, NgbTooltip],
})
export class IconWithTooltipComponent {
  readonly iconUseCase = input<IconUseCase>('tooltip_icon');
  readonly iconColor = input<IconColor>('default');

  getIconColor = computed(() => {
    if (this.iconColor() === 'danger') {
      return '#f44649';
    }
    if (this.iconColor() === 'success') {
      return '#177215';
    }
    return 'black';
  })
}
