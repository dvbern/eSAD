import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormArray, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RequirementSetting} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasHoerenEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {OtoskopieResultatSubFormType, UntersuchungHoerenOtoskopieForm} from './untersuchung-hoeren-otoskopie.form';
import {UntersuchungHoerenOtoskopieStore} from './untersuchung-hoeren-otoskopie.store';

@Component({
  selector: 'feat-untersuchung-hoeren-otoskopie',
  imports: [
    CommonModule, FormLayoutComponent, KachelControlComponent,
    KachelFormSectionComponent, ReactiveFormsModule, SaveCancelButtonsComponent,
    TranslocoPipe, CheckboxDirective, FormControlComponent,
    TextareaDirective, TitleComponent,
  ],
  templateUrl: './untersuchung-hoeren-otoskopie.component.html',
  styleUrl: './untersuchung-hoeren-otoskopie.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungHoerenOtoskopieComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;

  readonly hoerenOtoskopieStore = inject(UntersuchungHoerenOtoskopieStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);

  anlassConfig = computed(() => {
    if (this.anlassStore.selectedEntity() !== undefined) {
      return this.anlassStore.selectedEntity()?.anlassConfig;
    }
    return undefined;
  });

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasHoerenEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  form = new UntersuchungHoerenOtoskopieForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const hoerenOtoskopie = this.hoerenOtoskopieStore.hoerenOtoskopie();
      this.form.resetForm(hoerenOtoskopie?.otoskopieData);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidators(anlassConfig);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
    effect(() => {
      // Wenn die Default-Values zu applyen markiert sind, dann das Formular mit den neuen Werten laden
      const defaultHoerenOtoskopie = this.hoerenOtoskopieStore.defaultHoerenOtoskopieToApply();
      if (isNotNullish(defaultHoerenOtoskopie)) {
        const hasAnyValue = Object.values(defaultHoerenOtoskopie).some(isNotNullish);
        if (hasAnyValue) {
          this.form.resetForm(defaultHoerenOtoskopie);
          this.form.group.markAsDirty();
        }
        this.hoerenOtoskopieStore.resetApplyDefaultHoerenOtoskopie();
        this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      }
    });
  }

  loadDefault(): void {
    this.hoerenOtoskopieStore.applyDefaultHoerenOtoskopie();
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.hoerenOtoskopieStore.saveLoader.isLoading();
  }

  abbrechen(): void {
    const hoeren = this.hoerenOtoskopieStore.hoerenOtoskopie();
    if (hoeren) {
      this.form.resetForm(hoeren.otoskopieData);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.hoerenOtoskopieStore.saveHoerenOtoskopie({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungHoerenOtoskopieSaveJson: payload,
      });
    }
  }

  getOtoskopieRechtsArray(): FormArray<OtoskopieResultatSubFormType> {
    return this.form.group.controls.otoskopieRechtsArray;
  }

  getOtoskopieLinksArray(): FormArray<OtoskopieResultatSubFormType> {
    return this.form.group.controls.otoskopieLinksArray;
  }
}
