import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  DistanzSehenAugeEinzelnResultat,
  DistanzSehenResultat,
  FarbsehenResultat,
  InspektionKonvergenzResultat,
  MotilitaetResultat,
  RequirementSetting,
  SehenEmpfehlung,
  StereotestResultat,
  StrabismusAuftretenResultat,
  StrabismusAuspraegungResultat,
  VerwendetTafelResultat,
} from '@dvb/data-access-backend';
import {
  FormControlComponent,
  FormLayoutComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasSehenEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {hasSomeNotNullishValueIgnoringVersion} from '../../untersuchung.util';
import {UntersuchungSehenStore} from './../untersuchung-sehen.store';
import {UntersuchungSehenBeurteilungForm} from './untersuchung-sehen-beurteilung.form';
import {
  BenutzerStore,
  createMultiValueEnhancedForm,
  createSingleValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent,
} from "@dvb/feat-shared";

@Component({
  selector: 'feat-untersuchung-sehen-beurteilung',
  imports: [
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormLayoutComponent,
    KachelFormSectionComponent,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ValueEnhancedComponent
  ],
  templateUrl: './untersuchung-sehen-beurteilung.component.html',
  styleUrl: './untersuchung-sehen-beurteilung.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSehenBeurteilungComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungSehenStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly benutzerStore = inject(BenutzerStore);

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasSehenEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  distanzSehenAugeEinzelnItems = Object.values(DistanzSehenAugeEinzelnResultat);
  stereotestItems = Object.values(StereotestResultat);
  farbsehenItems = Object.values(FarbsehenResultat);
  verwendeteTafelItems = Object.values(VerwendetTafelResultat);
  motilitaetItems = Object.values(MotilitaetResultat);
  strabismusAuftretenItems = Object.values(StrabismusAuftretenResultat);
  strabismusAuspraegungItems = Object.values(StrabismusAuspraegungResultat);
  inspektionKonvergenzItems = Object.values(InspektionKonvergenzResultat);
  visusOptions = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0];
  distanzSehenItems = Object.values(DistanzSehenResultat);
  sehenEmpfehlungItems = Object.values(SehenEmpfehlung);

  strabismusItemsAnlassConfig = computed(() => this.anlassStore.selectedEntity()?.anlassConfig.data.sehenStrabismusValues
    || []);

  form = new UntersuchungSehenBeurteilungForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    brilleLinsenEnhancedForm: createSingleValueEnhancedForm(this.anlassStore.sehenBrilleLinsenValues()),
    stereotestEnhancedForm: createSingleValueEnhancedForm(this.stereotestItems),
    farbsehenEnhancedForm: createSingleValueEnhancedForm(this.farbsehenItems),
    verwendeteTafelEnhancedForm: createSingleValueEnhancedForm(this.verwendeteTafelItems),
    fernvisusAugeLinksEnhancedForm: createSingleValueEnhancedForm(this.distanzSehenAugeEinzelnItems),
    fernvisusAugeRechtsEnhancedForm: createSingleValueEnhancedForm(this.distanzSehenAugeEinzelnItems),
    motilitaetEnhancedForm: createSingleValueEnhancedForm(this.motilitaetItems),
    strabismusAuspraegungEnhancedForm: createSingleValueEnhancedForm(this.strabismusAuspraegungItems),
    strabismusAuftretenEnhancedForm: createSingleValueEnhancedForm(this.strabismusAuftretenItems),
    inspektionEnhancedForm: createSingleValueEnhancedForm(this.inspektionKonvergenzItems),
    konvergenzEnhancedForm: createSingleValueEnhancedForm(this.inspektionKonvergenzItems),
    fernvisusEnhancedForm: createMultiValueEnhancedForm(this.distanzSehenItems),
    nahvisusEnhancedForm: createMultiValueEnhancedForm(this.distanzSehenItems),
    strabismusEnhancedForm: createMultiValueEnhancedForm(this.strabismusItemsAnlassConfig()),
    empfehlungEnhancedForm: createMultiValueEnhancedForm(this.sehenEmpfehlungItems),
  });

  constructor() {
    effect(() => {
      const sehen = this.store.sehenBeurteilung();
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;

      this.form.resetForm(sehen?.data);
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, !!this.tenantSettingsStore.nichtGeprueftGrundEnabled());
        });
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
    });
    effect(() => {
      // Wenn die Default-Values zu applyen markiert sind, dann das Formular mit den neuen Werten laden
      const defaultSehen = this.store.defaultSehenBeurteilungToApply();

      if (isNotNullish(defaultSehen)) {
        const hasAnyValue = hasSomeNotNullishValueIgnoringVersion(defaultSehen);
        if (hasAnyValue) {
          this.form.resetForm(defaultSehen);
          this.form.group.markAsDirty();
        }
        this.store.resetApplyDefaultSehenBeurteilung();
        this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      }
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const sehen = this.store.sehenBeurteilung();
      this.form.resetForm(sehen?.data);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const payload = this.form.valueOnValid();
      this.store.saveSehenBeurteilung({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungSehenBeurteilungSaveJson: payload,
      });
    }
  }

  loadDefault(): void {
    this.store.applyDefaultSehenBeurteilung();
  }
}
