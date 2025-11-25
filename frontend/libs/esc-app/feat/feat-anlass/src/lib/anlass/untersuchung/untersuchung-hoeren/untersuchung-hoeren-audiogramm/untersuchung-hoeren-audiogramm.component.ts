import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormArray, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RequirementSetting} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  KachelControlComponent,
  KachelFormSectionComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasHoerenEditBerechtigung, isNotNullish, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {hasSomeNotNullishValueIgnoringVersion} from '../../untersuchung.util';

import {AudiogrammForm} from './audiogramm/audiogramm-form';
import {getAllAudiogrammKeys} from './audiogramm/audiogramm-util';
import {AudiogrammComponent} from './audiogramm/audiogramm.component';
import {
  AudiogrammBeurteilungSubFormType,
  EmpfehlungSubFormType,
  UntersuchungHoerenAudiogrammForm,
  ZusaetzlicheAngabenSubFormType,
} from './untersuchung-hoeren-audiogramm.form';
import {UntersuchungHoerenAudiogrammStore} from './untersuchung-hoeren-audiogramm.store';

@Component({
  selector: 'feat-untersuchung-hoeren-audiogramm',
  imports: [
    FormLayoutComponent,
    HeadingDirective,
    KachelControlComponent,
    KachelFormSectionComponent,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    CheckboxDirective,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    AudiogrammComponent,
  ],
  templateUrl: './untersuchung-hoeren-audiogramm.component.html',
  styleUrl: './untersuchung-hoeren-audiogramm.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungHoerenAudiogrammComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;

  readonly hoerenAudiogrammStore = inject(UntersuchungHoerenAudiogrammStore);
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

  public audiogrammForm: AudiogrammForm = new AudiogrammForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    frequenzenKeys: getAllAudiogrammKeys(),
    anlassConfigSig: this.anlassConfig,
  });

  form = new UntersuchungHoerenAudiogrammForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    audiogrammForm: this.audiogrammForm,
  });

  displayAudiogramm(): boolean {
    const config = this.anlassConfig();
    if (isNullish(config)) {
      return false;
    }
    return !(config.data.hoerenAudiogramm === RequirementSetting.NOT_VISIBLE
      && config.data.hoerenAudiogramm250Hz === RequirementSetting.NOT_VISIBLE
      && config.data.hoerenAudiogramm8000Hz === RequirementSetting.NOT_VISIBLE);
  }

  constructor() {
    effect(() => {
      const hoeren = this.hoerenAudiogrammStore.hoerenAudiogramm();
      this.form.resetForm(hoeren?.audiogrammData);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidators(anlassConfig, hoeren?.audiogrammData);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });

    effect(() => {
      // Wenn die Default-Values zu applyen markiert sind, dann das Formular mit den neuen Werten laden
      const defaultHoerenAudiogramm = this.hoerenAudiogrammStore.defaultHoerenAudiogrammToApply();
      if (isNotNullish(defaultHoerenAudiogramm)) {
        const hasAnyValue = hasSomeNotNullishValueIgnoringVersion(defaultHoerenAudiogramm);
        if (hasAnyValue) {
          this.form.resetForm(defaultHoerenAudiogramm);
          this.form.group.markAsDirty();
        }
        this.hoerenAudiogrammStore.resetApplyDefaultHoeren();
        this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      }
    });
  }

  loadDefault(): void {
    this.hoerenAudiogrammStore.applyDefaultHoeren();
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.hoerenAudiogrammStore.saveLoader.isLoading();
  }

  abbrechen(): void {
    const hoeren = this.hoerenAudiogrammStore.hoerenAudiogramm();
    if (hoeren) {
      this.form.resetForm(hoeren.audiogrammData);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.hoerenAudiogrammStore.saveHoerenAudiogramm({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungHoerenAudiogrammSaveJson: payload,
      });
    }
  }

  getAudiogrammBeurteilungArray(): FormArray<AudiogrammBeurteilungSubFormType> {
    return this.form.group.controls.audiogrammBeurteilungArray;
  }

  getZusaetzlicheAngabenArray(): FormArray<ZusaetzlicheAngabenSubFormType> {
    return this.form.group.controls.zusaetzlicheAngabenArray;
  }

  getEmpfehlungFormArray(): FormArray<EmpfehlungSubFormType> {
    return this.form.group.controls.empfehlungArray;
  }
}
