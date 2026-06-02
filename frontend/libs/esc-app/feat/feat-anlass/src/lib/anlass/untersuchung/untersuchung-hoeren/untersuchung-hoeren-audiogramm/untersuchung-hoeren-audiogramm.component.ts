import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  AudiogrammBeurteilung,
  AudiogrammSimpel,
  HoerenEmpfehlung,
  HoerenHoerhilfe,
  HoerenZusaetzlicheAngaben,
  RequirementSetting
} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  createMultiValueEnhancedForm,
  createSingleValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent,
} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
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
import {UntersuchungHoerenAudiogrammForm} from './untersuchung-hoeren-audiogramm.form';
import {UntersuchungHoerenAudiogrammStore} from './untersuchung-hoeren-audiogramm.store';

@Component({
  selector: 'feat-untersuchung-hoeren-audiogramm',
  imports: [
    FormLayoutComponent,
    HeadingDirective,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    AudiogrammComponent,
    ValueEnhancedComponent,
  ],
  templateUrl: './untersuchung-hoeren-audiogramm.component.html',
  styleUrl: './untersuchung-hoeren-audiogramm.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungHoerenAudiogrammComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;
  readonly AudiogrammSimpel = Object.values(AudiogrammSimpel);
  readonly HoerenHoerhilfe = Object.values(HoerenHoerhilfe);

  readonly hoerenAudiogrammStore = inject(UntersuchungHoerenAudiogrammStore);
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

  audiogrammBeurteilungItems = Object.values(AudiogrammBeurteilung);
  hoerenZusaetzlicheAngabenItems = Object.values(HoerenZusaetzlicheAngaben);
  hoerenEmpfehlungItems = Object.values(HoerenEmpfehlung);

  public audiogrammForm: AudiogrammForm = new AudiogrammForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    frequenzenKeys: getAllAudiogrammKeys(),
    anlassConfigSig: this.anlassConfig,
  });

  form = new UntersuchungHoerenAudiogrammForm({
    fb: inject(NonNullableFormBuilder),
    audiogrammSimpelValueEnhancedForm: createSingleValueEnhancedForm(this.AudiogrammSimpel),
    hoerhilfeValueEnhancedForm: createSingleValueEnhancedForm(this.HoerenHoerhilfe),
    audiogrammBeurteilungValueEnhancedForm: createMultiValueEnhancedForm(this.audiogrammBeurteilungItems),
    zusaetzlicheAngabenValueEnhancedForm: createMultiValueEnhancedForm(this.hoerenZusaetzlicheAngabenItems),
    empfehlungValueEnhancedForm: createMultiValueEnhancedForm(this.hoerenEmpfehlungItems),
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
          this.form.setValidators(anlassConfig,
            hoeren?.audiogrammData,
            !!this.tenantSettingsStore.nichtGeprueftGrundEnabled());
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
          console.log(defaultHoerenAudiogramm);
          this.form.resetForm(defaultHoerenAudiogramm);
          this.form.group.markAsDirty();
          this.form.group.controls.audiogrammBeurteilung.controls.valueArray.markAsDirty();
          this.form.group.controls.empfehlung.controls.valueArray.markAsDirty();
          this.form.group.controls.zusaetzlicheAngaben.controls.valueArray.markAsDirty();
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
    this.form.resetForm(this.hoerenAudiogrammStore.hoerenAudiogramm()?.audiogrammData ?? undefined);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.getWrappedEinladungId());

      this.hoerenAudiogrammStore.saveHoerenAudiogramm({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungHoerenAudiogrammSaveJson: payload,
      });
    }
  }
}
