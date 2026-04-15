import { ChangeDetectionStrategy, Component, HostBinding } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import {
  ControlType,
  ValidatedInputWithIdDirective,
} from '../validated-input-with-id.directive';

export interface SelectItem {
  value: string;
  label: string;
}

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'select[svuiSelect]',
    templateUrl: './select.component.html',
    styleUrl: './select.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            // See comment on ValidatedInputWithIdDirective for reasons
            provide: ValidatedInputWithIdDirective,
            useExisting: SelectComponent,
        },
    ],
    imports: [ReactiveFormsModule]
})
export class SelectComponent extends ValidatedInputWithIdDirective {
  @HostBinding('class.form-select') readonly bootstrapClass = true;

  get controlType(): ControlType {
    return 'select';
  }

  get isLabelFloatable(): boolean {
    return true;
  }
}
