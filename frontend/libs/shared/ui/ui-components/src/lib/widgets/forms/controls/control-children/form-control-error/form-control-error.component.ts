import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { ValidationErrors } from '@angular/forms';
import { Nullish } from '@dvb/util-shared';
import { FormErrorComponent } from './form-error/form-error.component';
import { SharedUiControlMessagePipe } from './control-message.pipe';

// FIXME: *sehr* hacky implementierung, aber die Idee ist damit mal sichtbar
@Component({
    selector: 'svui-form-control-error',
    templateUrl: './form-control-error.component.html',
    styleUrl: './form-control-error.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [FormErrorComponent]
})
export class FormControlErrorComponent {
  #pipe = new SharedUiControlMessagePipe();

  readonly errors = input.required<ValidationErrors | Nullish>();

  errorMessage = computed(() => {
    return this.#pipe.transform(this.errors());
  });
}
