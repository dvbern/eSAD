import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {IconComponent, IconUseCase} from '../../icons';

@Component({
  selector: 'svui-input-with-icon',
  templateUrl: './input-with-icon.component.html',
  styleUrls: ['./input-with-icon.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IconComponent,
  ],
})
export class InputWithIconComponent {
  readonly useCase = input.required<IconUseCase>();
  readonly tooltip = input<string>();
}
