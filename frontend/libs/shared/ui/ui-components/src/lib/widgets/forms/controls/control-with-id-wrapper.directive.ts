import { ContentChild, Directive } from '@angular/core';
import { isNullish, requireValue } from '@dvb/util-shared';
import { ValidatedInputWithIdDirective } from '../inputs/validated-input-with-id.directive';
import { ValidatedControlWrapperDirective } from './validated-control-wrapper.directive';

@Directive({ standalone: true })
export abstract class ControlWithIdWrapperDirective extends ValidatedControlWrapperDirective {
  #id!: string;

  get id(): string {
    return this.#id;
  }

  @ContentChild(ValidatedInputWithIdDirective)
  set id(directiveOnContent: ValidatedInputWithIdDirective | unknown) {
    if (isNullish(directiveOnContent)) {
      // gets called multiple times
      this.#id = 'id-not-set';
      return;
    }

    if (!(directiveOnContent instanceof ValidatedInputWithIdDirective)) {
      throw new Error('FormControl needs an InputDirective as ContentChild');
    }

    this.#id = requireValue(directiveOnContent.id, () => 'Control ID is required');
  }
}
