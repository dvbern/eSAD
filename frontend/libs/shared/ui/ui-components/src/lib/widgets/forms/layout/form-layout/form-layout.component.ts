import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DetailMode } from '@dvb/util-angular';

export type FormType = 'default' | 'kacheln' | 'uploadConfig';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: '[svuiFormLayout]',
  standalone: true,
  templateUrl: './form-layout.component.html',
  styleUrl: './form-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormLayoutComponent {
  mode = input.required<DetailMode>();
  formType = input<FormType>('default');
}
