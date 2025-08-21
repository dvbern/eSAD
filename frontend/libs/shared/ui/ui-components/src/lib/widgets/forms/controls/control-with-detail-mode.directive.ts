import { computed, Directive, Optional } from '@angular/core';
import { DetailMode } from '@dvb/util-angular';
import { FormLayoutComponent } from '../layout';
import { ControlWithIdWrapperDirective } from './control-with-id-wrapper.directive';

@Directive({
  standalone: true,
})
export class ControlWithDetailModeDirective extends ControlWithIdWrapperDirective {
  // will be set by the directive on the content
  #formcomponent?: FormLayoutComponent;
  isEditMode = computed(() => this.#formcomponent?.mode() === DetailMode.EDIT);

  constructor(@Optional() formComponent?: FormLayoutComponent) {
    super();
    if (formComponent) {
      this.#formcomponent = formComponent;
    }
  }
}
