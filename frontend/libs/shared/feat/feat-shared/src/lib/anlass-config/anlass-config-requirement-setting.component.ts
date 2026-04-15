import { ChangeDetectionStrategy, Component, HostBinding, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { FormControlComponent, NgSelectDirective } from '@dvb/ui-components';
import { TranslocoPipe } from '@jsverse/transloco';
import { RequirementSetting } from '@dvb/data-access-backend';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';

@Component({
  selector: 'feat-anlass-config-requirement-setting',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    ReactiveFormsModule,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
  ],
  templateUrl: './anlass-config-requirement-setting.component.html',
})
export class AnlassConfigRequirementSettingComponent {
  @HostBinding('class.svui_form-layout--normal') readonly layoutNormal = true;

  formControlAttr = input.required<FormControl<RequirementSetting>>();
  labelKey = input.required<string>();

  protected readonly requirementSettingOptions = Object.values(RequirementSetting);
  protected readonly RequirementSetting = RequirementSetting;
}
