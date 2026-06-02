import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  AnlassConfigJson, AnlassConfigSaveJson,
  Geschlecht,
  PerzentileGewicht,
  PerzentileGroesse,
  RequirementSetting,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  BenutzerStore,
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
import {PercentileDiagramConfigService} from './config/percentile-diagram-config.service';
import {UntersuchungGroesseGewichtForm} from './untersuchung-groessegewicht.form';
import {UntersuchungGroesseGewichtStore} from './untersuchung-groessegewicht.store';

@Component({
  selector: 'feat-untersuchung-groessegewicht',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    KachelFormSectionComponent,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    SaveCancelButtonsComponent,
    TextareaDirective,
    TitleComponent,
    PercentileChartComponent,
    CardInCardComponent,
    ValueEnhancedComponent,
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
  showDiagarammFemale = computed((): boolean => {
    if (this.anlassStore.isAnonymous()) {
      return false;
    }
    const currentKind = this.kind();
    return isNotNullish(currentKind?.geschlecht) && currentKind?.geschlecht !== Geschlecht.MAENNLICH;
  });

  showDiagarammMale = computed((): boolean =>  {
    if (this.anlassStore.isAnonymous()) {
      return false;
    }
    const currentKind = this.kind();
    return isNotNullish(currentKind?.geschlecht) && currentKind?.geschlecht !== Geschlecht.WEIBLICH;
  });

  showDiagrammGroesse = computed((): boolean =>
    isSettingVisible(this.anlassStore.selectedEntity()?.anlassConfig,'groesseGewichtGroesse')
  );

  showDiagrammGewicht = computed((): boolean =>
    isSettingVisible(this.anlassStore.selectedEntity()?.anlassConfig, 'groesseGewichtGewicht')
  );

  showDiagrammBmi = computed((): boolean =>
    isSettingVisible(this.anlassStore.selectedEntity()?.anlassConfig, 'groesseGewichtBmi')
  );

  perzentileGroesse = Object.values(PerzentileGroesse);
  perzentileGewicht = Object.values(PerzentileGewicht);

  form = new UntersuchungGroesseGewichtForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    diagramConfigService: this.diagramConfigService,
    groessePerzentileValueEnhancedForm: createSingleValueEnhancedForm(this.perzentileGroesse),
    gewichtPerzentileValueEnhancedForm: createSingleValueEnhancedForm(this.perzentileGewicht),
    bmiPerzentileValueEnhancedForm: createSingleValueEnhancedForm(this.anlassStore.groesseGewichtBmiPerzentileValues()),
  });

  constructor() {
    effect(() => {
      const value = this.groesseGewichtStore.groesseGewicht();
      this.form.resetForm(value, this.alter(), this.alterDuodezimal());
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig);
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
 function isSettingVisible(anlassConfig: AnlassConfigJson | undefined, property: keyof  AnlassConfigSaveJson) : boolean {
   return isNotNullish(anlassConfig) && anlassConfig.data[property] !== RequirementSetting.NOT_VISIBLE;
 }
