import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormArray, ReactiveFormsModule } from '@angular/forms';
import {
  CheckboxDirective,
  FormControlComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  RadioDirective,
} from '@dvb/ui-components';
import { TranslocoPipe } from '@jsverse/transloco';
import { MultiValueSubFormType, ValueEnhancedForm } from './value-enhanced.form';
import { NichtGeprueftGrund, RequirementSetting } from '@dvb/data-access-backend';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';

@Component({
  selector: 'feat-value-enhanced',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    ReactiveFormsModule,
    KachelControlComponent,
    KachelFormSectionComponent,
    RadioDirective,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    CheckboxDirective,
  ],
  templateUrl: './value-enhanced.component.html',
})
export class ValueEnhancedComponent {
  private static nextRadioGroupId = 0;
  readonly radioGroupId = ValueEnhancedComponent.nextRadioGroupId++;

  form = input.required<ValueEnhancedForm>();
  valueTitleKey = input.required<string>();
  valuePrefix = input.required<string>();
  nichtGeprueftGrundEnabled = input.required<boolean | undefined>();

  readonly nichtGeprueftGrundValues = Object.values(NichtGeprueftGrund);
  protected readonly RequirementSetting = RequirementSetting;

  getMultiValueArray(): FormArray<MultiValueSubFormType> {
    return this.form().group.controls.valueArray;
  }

  getRadioGroupName(): string {
    return `${this.valuePrefix()}-${this.radioGroupId}`;
  }
}
