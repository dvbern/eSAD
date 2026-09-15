import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Geschlecht, RequirementSetting,} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  BenutzerStore,
  createMultiValueEnhancedForm,
  createSingleValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent
} from '@dvb/feat-shared';
import {
  CardInCardComponent,
  FormControlComponent,
  FormLayoutComponent,
  KachelFormSectionComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TextInputDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasGroesseGewichtEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {PercentileChartComponent} from '@dvb/viz';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass/anlass.store';
import {AnlassKinderStore} from '../../anlasse-detail/anlass-kinder.store';
import {UntersuchungStore} from '../untersuchung.store';
import {PercentileReference, percentileReferenceForAnlass} from './config/percentile-curve-sets';
import {PercentileDiagramConfigService} from './config/percentile-diagram-config.service';
import {UntersuchungGroesseGewichtForm} from './untersuchung-groessegewicht.form';
import {UntersuchungGroesseGewichtStore} from './untersuchung-groessegewicht.store';

@Component({
  selector: 'feat-untersuchung-groessegewicht',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    SaveCancelButtonsComponent,
    TextareaDirective,
    TitleComponent,
    PercentileChartComponent,
    CardInCardComponent,
    ValueEnhancedComponent,
    KachelFormSectionComponent,
  ],
  templateUrl: './untersuchung-groessegewicht.component.html',
  styleUrl: './untersuchung-groessegewicht.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungGroessegewichtComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;
  protected readonly Geschlecht = Geschlecht;

  readonly anlassStore = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly groesseGewichtStore = inject(UntersuchungGroesseGewichtStore);
  readonly diagramConfigService = inject(PercentileDiagramConfigService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  kind = computed(() => {
    const currentEinladungId: BackendEntityId | undefined = this.untersuchungStore.currentTab()?.einladungID;
    if (currentEinladungId) {
      return this.anlassKinderStore.einladungById(currentEinladungId)?.kind.data;
    }
    return undefined;
  });

  alterDuodezimal = computed(() => {
    return this.groesseGewichtStore.groesseGewicht()?.alterDuodezimal;
  });

  alter = computed(() => {
    return this.groesseGewichtStore.groesseGewicht()?.alter;
  });

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasGroesseGewichtEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());
  showDiagrammFemale = computed((): boolean => {
    if (this.anlassStore.isAnonymous()) {
      return false;
    }
    const currentKind = this.kind();
    return isNotNullish(currentKind?.geschlecht) && currentKind?.geschlecht !== Geschlecht.MAENNLICH;
  });

  showDiagrammMale = computed((): boolean => {
    if (this.anlassStore.isAnonymous()) {
      return false;
    }
    const currentKind = this.kind();
    return isNotNullish(currentKind?.geschlecht) && currentKind?.geschlecht !== Geschlecht.WEIBLICH;
  });

  // Perzentilen-Kurven werden immer angezeigt, ausser bei anonymen Anlaessen
  // (unabhaengig von Freitext- bzw. Perzentilen-Settings).
  showDiagrammGroesse = computed((): boolean => !this.anlassStore.isAnonymous());

  showDiagrammGewicht = computed((): boolean => !this.anlassStore.isAnonymous());

  showDiagrammBmi = computed((): boolean => !this.anlassStore.isAnonymous());

  // Der Titel (Groesse/Gewicht/BMI) soll pro Wert nur einmal angezeigt werden - unabhaengig davon,
  // ob Freitext, Single-Value und/oder Multi-Value konfiguriert sind. Er wird angezeigt, sobald
  // mindestens eines der drei zugehoerigen Settings nicht auf NOT_VISIBLE steht.
  showTitleGroesse = computed((): boolean => {
    const config = this.anlassStore.selectedEntity()?.anlassConfig?.data;
    return [
      config?.groesseGewichtGroesse,
      config?.groesseGewichtGroessePerzentileSingleValue,
      config?.groesseGewichtGroessePerzentileMultiValue,
    ].some(setting => setting !== undefined && setting !== RequirementSetting.NOT_VISIBLE);
  });

  showTitleGewicht = computed((): boolean => {
    const config = this.anlassStore.selectedEntity()?.anlassConfig?.data;
    return [
      config?.groesseGewichtGewicht,
      config?.groesseGewichtGewichtPerzentileSingleValue,
      config?.groesseGewichtGewichtPerzentileMultiValue,
    ].some(setting => setting !== undefined && setting !== RequirementSetting.NOT_VISIBLE);
  });

  showTitleBmi = computed((): boolean => {
    const config = this.anlassStore.selectedEntity()?.anlassConfig?.data;
    return [
      config?.groesseGewichtBmi,
      config?.groesseGewichtBmiPerzentileSingleValue,
      config?.groesseGewichtBmiPerzentileMultiValue,
    ].some(setting => setting !== undefined && setting !== RequirementSetting.NOT_VISIBLE);
  });

  /** The reference curves in force at the time of the Anlass. */
  percentileReference = computed((): PercentileReference =>
    percentileReferenceForAnlass(
      requireValue(this.tenantSettingsStore.settings()?.data.gueltigAbWachstumskurvePch),
      this.anlassStore.selectedEntity()?.data.datum
    )
  );

  form = new UntersuchungGroesseGewichtForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    diagramConfigService: this.diagramConfigService,
    groesseSingleValueForm: createSingleValueEnhancedForm(this.anlassStore.groesseGewichtGroessePerzentileValues()),
    groesseMultiValueForm: createMultiValueEnhancedForm(this.anlassStore.groesseGewichtGroessePerzentileValues()),
    gewichtSingleValueForm: createSingleValueEnhancedForm(this.anlassStore.groesseGewichtGewichtPerzentileValues()),
    gewichtMultiValueForm: createMultiValueEnhancedForm(this.anlassStore.groesseGewichtGewichtPerzentileValues()),
    bmiSingleValueForm: createSingleValueEnhancedForm(this.anlassStore.groesseGewichtBmiPerzentileValues()),
    bmiMultiValueForm: createMultiValueEnhancedForm(this.anlassStore.groesseGewichtBmiPerzentileValues()),
  });

  constructor() {
    effect(() => {
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        // Die Perzentilen-Optionslisten muessen vor dem Reset aktualisiert werden, da die Config
        // (z.B. bei einem Hard-Reload) erst nachtraeglich vom Server eintreffen kann.
        this.form.updatePerzentileOptions(anlassConfig);
      }
      const value = this.groesseGewichtStore.groesseGewicht();
      // Before resetForm, so the data points are put onto the charts of the right reference.
      this.form.setReference(this.percentileReference());
      this.form.resetForm(value, this.alter(), this.alterDuodezimal());
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, !!this.tenantSettingsStore.nichtGeprueftGrundEnabled());
        });
      }
      this.form.setDisabled(!this.canEdit())
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.groesseGewichtStore.saveLoader.isLoading();
  }

  abbrechen(): void {
    const data = this.groesseGewichtStore.groesseGewicht();
    this.form.resetForm(data, this.alter(), this.alterDuodezimal());
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      this.groesseGewichtStore.saveGroesseGewicht({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungGroesseGewichtSaveJson: payload,
      });
    }
  }
}
