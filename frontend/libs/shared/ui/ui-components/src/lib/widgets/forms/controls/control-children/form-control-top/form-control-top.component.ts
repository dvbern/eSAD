import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {IconComponent, IconUseCase} from '../../../../icons/icon/icon.component';

@Component({
  selector: 'svui-form-control-top',
  standalone: true,
  templateUrl: './form-control-top.component.html',
  styleUrl: './form-control-top.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IconComponent,
  ],
})
export class FormControlTopComponent {
  readonly minHeight = input.required<boolean>();
  readonly iconTopRight = input<{ iconTooltip: string, icon: IconUseCase }>();
}
