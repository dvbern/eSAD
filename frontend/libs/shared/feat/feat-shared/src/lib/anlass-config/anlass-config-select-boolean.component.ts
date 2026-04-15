import { ChangeDetectionStrategy, Component, HostBinding, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { FormControlComponent, NgSelectDirective } from '@dvb/ui-components';
import { TranslocoPipe } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';

@Component({
  selector: 'feat-anlass-config-select-boolean',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    ReactiveFormsModule,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
  ],
  templateUrl: './anlass-config-select-boolean.component.html',
})
export class AnlassConfigSelectBooleanComponent {
  @HostBinding('class.svui_form-layout--normal') readonly layoutNormal = true;

  formControlAttr = input.required<FormControl<boolean>>();
  labelKey = input.required<string>();
}
