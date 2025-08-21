import { Directive, input } from '@angular/core';
import { ButtonBase } from './button-base.directive';

export type ButtonType = 'button' | 'submit' | 'reset';

@Directive({ standalone: true })
export abstract class ButtonElement extends ButtonBase {
  readonly type = input.required<ButtonType>();
}
