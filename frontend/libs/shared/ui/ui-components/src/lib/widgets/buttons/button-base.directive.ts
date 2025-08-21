import { Directive, HostBinding, input } from '@angular/core';
import { IconUseCase } from '../icons/icon';
import {
  bootstrapButtonColorStyleClass,
  ButtonType,
} from './bootstrap-button-color';

@Directive({ standalone: true })
export abstract class ButtonBase {
  readonly icon = input<IconUseCase | undefined>(undefined);
  readonly working = input<boolean>(false);

  #buttonType!: ButtonType;

  @HostBinding('class.btn') private readonly bootstrapClass = true;
  @HostBinding('class') get buttonType(): string {
    return bootstrapButtonColorStyleClass(this.#buttonType);
  }

  protected set buttonType(buttonColor: ButtonType) {
    this.#buttonType = buttonColor;
  }

  @HostBinding('class.with-icon') get hasIcon(): boolean {
    return this.icon() !== undefined;
  }

}
