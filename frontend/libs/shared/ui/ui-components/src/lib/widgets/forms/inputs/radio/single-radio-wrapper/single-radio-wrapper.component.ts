import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ControlWithDetailModeDirective } from '../../../controls/control-with-detail-mode.directive';

@Component({
  selector: 'svui-single-radio-wrapper',
  standalone: true,
  templateUrl: './single-radio-wrapper.component.html',
  styleUrl: './single-radio-wrapper.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SingleRadioWrapperComponent extends ControlWithDetailModeDirective {
  readonly label = input.required<string>();
}
