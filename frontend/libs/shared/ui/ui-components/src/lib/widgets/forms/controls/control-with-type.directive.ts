import { ContentChild, Directive, input, model } from '@angular/core';
import { ControlWithDetailModeDirective } from './control-with-detail-mode.directive';
import { ControlType, ValidatedInputWithIdDirective } from '../inputs/validated-input-with-id.directive';
import { HeadingVisibility } from '../../headings/with-accessible-heading/with-accessible-heading.directive';

@Directive({
  standalone: true,
})
export class ControlWithTypeDirective extends ControlWithDetailModeDirective {
  readonly label = input.required<string>();
  readonly topLabelVisibility = input<HeadingVisibility>('visible');

  // will be set by the directive on the content
  directiveOnContentWorking = model<boolean>(false);
  #controlType: ControlType = 'textInput';

  get controlType(): ControlType {
    return this.#controlType;
  }

  @ContentChild(ValidatedInputWithIdDirective)
  set controlConfig(directiveOnContent: ValidatedInputWithIdDirective | undefined) {
    if (directiveOnContent) {
      this.#controlType = directiveOnContent.controlType;
      this.directiveOnContentWorking = directiveOnContent.working;
    }
  }
}
