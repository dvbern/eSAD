import {ChangeDetectionStrategy, Component, computed, input, Signal} from '@angular/core';
import {FormArray, ReactiveFormsModule} from '@angular/forms';
import {
  CheckboxDirective,
  FormControlComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  RadioDirective,
  SingleValueCheckboxDirective,
} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {MultiValueSubFormType, ValueEnhancedForm} from './value-enhanced.form';
import {NichtGeprueftGrund, RequirementSetting} from '@dvb/data-access-backend';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {startWith, switchMap} from 'rxjs';

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
    SingleValueCheckboxDirective,
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

  /**
   * Reaktives Signal für das gesamte Form-valueChanges. Wird benötigt, damit
   * z.B. `isNichtGeprueftChecked()` (das auch auf den Multi-valueArray zugreift)
   * im Template korrekt aktualisiert wird, wenn der Form-Wert programmatisch
   * gesetzt wird.
   */
  readonly formValue = toSignal(
    toObservable(this.form).pipe(
      switchMap(form => form.group.valueChanges.pipe(startWith(form.group.value))),
    ),
  );

  readonly isNichtGeprueftChecked: Signal<boolean> = computed(() => {
    // Abhängigkeit von formValue sicherstellen, damit das computed bei Änderungen neu evaluiert wird
    this.formValue();
    return this.form().isNichtGeprueftChecked();
  });

  getMultiValueArray(): FormArray<MultiValueSubFormType> {
    return this.form().group.controls.valueArray;
  }

  getRadioGroupName(): string {
    return `${this.valuePrefix()}-${this.radioGroupId}`;
  }
}
