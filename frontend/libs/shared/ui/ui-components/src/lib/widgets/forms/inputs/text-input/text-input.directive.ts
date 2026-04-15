import { Directive, HostBinding, Input } from '@angular/core';
import {
  ControlType,
  ValidatedInputWithIdDirective,
} from '../validated-input-with-id.directive';

export type SupportedInputType = 'text' | 'email' | 'date' | 'time' | 'tel' | 'month' | 'number';

@Directive({
  selector: 'input[svuiTextInput]',
  standalone: true,
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: TextInputDirective,
    },
  ],
})
export class TextInputDirective extends ValidatedInputWithIdDirective {
  @HostBinding('class.form-control') readonly bootstrapClass = true;
  private readonly defaultInputType: SupportedInputType = 'text';
  #inputType!: SupportedInputType;

  get controlType(): ControlType {
    return 'textInput';
  }

  get isLabelFloatable(): boolean {
    return true;
  }

  @HostBinding('type') get inputType(): string {
    return this.#inputType;
  }

  @Input() set svuiTextInput(type: SupportedInputType | '') {
    this.#inputType = type === '' ? this.defaultInputType : type;
  }
}
