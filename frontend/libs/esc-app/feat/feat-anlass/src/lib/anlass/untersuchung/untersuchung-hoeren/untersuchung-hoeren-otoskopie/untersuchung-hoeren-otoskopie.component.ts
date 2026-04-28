
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {OtoskopieResultat, RequirementSetting} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  createMultiValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent
} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {toEinladungBackendEntityIdWithClass} from '@dvb/data-access-types';
import {DirtyMarker} from '@dvb/util-angular';
import {hasHoerenEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {hasSomeNotNullishValueIgnoringVersion} from '../../untersuchung.util';
import {UntersuchungHoerenOtoskopieForm} from './untersuchung-hoeren-otoskopie.form';
import {UntersuchungHoerenOtoskopieStore} from './untersuchung-hoeren-otoskopie.store';

@Component({
  selector: 'feat-untersuchung-hoeren-otoskopie',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    ValueEnhancedComponent
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
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  anlassConfig = computed(() => {
    if (this.anlassStore.selectedEntity() !== undefined) {
      return this.anlassStore.selectedEntity()?.anlassConfig;
    }
    return undefined;
  });

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasHoerenEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  readonly otoskopieItems = Object.values(OtoskopieResultat);

  form = new UntersuchungHoerenOtoskopieForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    otoskopieLinksValueEnhancedForm: createMultiValueEnhancedForm(this.otoskopieItems),
    otoskopieRechtsValueEnhancedForm: createMultiValueEnhancedForm(this.otoskopieItems)
  });

  constructor() {
    effect(() => {
      const hoerenOtoskopie = this.hoerenOtoskopieStore.hoerenOtoskopie();
      this.form.resetForm(hoerenOtoskopie?.otoskopieData);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      const nichtGeprueftGrundEnabled = this.tenantSettingsStore.nichtGeprueftGrundEnabled();
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidators(anlassConfig, !!nichtGeprueftGrundEnabled);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
    effect(() => {
      // Wenn die Default-Values zu applyen markiert sind, dann das Formular mit den neuen Werten laden
      const defaultHoerenOtoskopie = this.hoerenOtoskopieStore.defaultHoerenOtoskopieToApply();
      if (isNotNullish(defaultHoerenOtoskopie)) {
        const hasAnyValue = hasSomeNotNullishValueIgnoringVersion(defaultHoerenOtoskopie);
        if (hasAnyValue) {
          this.form.resetForm(defaultHoerenOtoskopie);
          this.form.group.markAsDirty();
          this.form.group.controls.otoskopieRechts.controls.valueArray.markAsDirty();
          this.form.group.controls.otoskopieLinks.controls.valueArray.markAsDirty();
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
        einladungId: toEinladungBackendEntityIdWithClass(einladungId),
        untersuchungHoerenOtoskopieSaveJson: payload,
      });
    }
  }
}
