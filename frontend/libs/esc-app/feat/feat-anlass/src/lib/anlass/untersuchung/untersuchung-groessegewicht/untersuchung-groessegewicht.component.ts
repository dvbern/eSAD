import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  Geschlecht,
  PerzentileBmi,
  PerzentileGewicht,
  PerzentileGroesse,
  RequirementSetting,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  CardInCardComponent,
  FormControlComponent,
  FormLayoutComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  RadioDirective,
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
    KachelControlComponent,
    KachelFormSectionComponent,
    RadioDirective,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    SaveCancelButtonsComponent,
    TextareaDirective,
    TitleComponent,
    PercentileChartComponent,
    CardInCardComponent,
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

  perzentileGroesse = Object.values(PerzentileGroesse);
  perzentileGewicht = Object.values(PerzentileGewicht);
  perzentileBmi = Object.values(PerzentileBmi);

  form = new UntersuchungGroesseGewichtForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    diagramConfigService: this.diagramConfigService,
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

  showDiagarammFemale(): boolean {
    const currentKind = this.kind();
    return isNotNullish(currentKind?.geschlecht) && currentKind?.geschlecht !== Geschlecht.MAENNLICH;
  }

  showDiagarammMale(): boolean {
    const currentKind = this.kind();
    return isNotNullish(currentKind?.geschlecht) && currentKind?.geschlecht !== Geschlecht.WEIBLICH;
  }

  showDiagrammGroesse(): boolean {
    const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
    return isNotNullish(anlassConfig) && anlassConfig.data.groesseGewichtGroesse !== RequirementSetting.NOT_VISIBLE;
  }

  showDiagrammGewicht(): boolean {
    const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
    return isNotNullish(anlassConfig)
      && this.anlassStore.selectedEntity()?.anlassConfig?.data.groesseGewichtGewicht
      !== RequirementSetting.NOT_VISIBLE;
  }

  showDiagrammBmi(): boolean {
    const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
    return isNotNullish(anlassConfig)
      && this.anlassStore.selectedEntity()?.anlassConfig?.data.groesseGewichtBmi
      !== RequirementSetting.NOT_VISIBLE;
  }

  abbrechen(): void {
    const data = this.groesseGewichtStore.groesseGewicht();
    this.form.resetForm(data, this.alter(), this.alterDuodezimal());
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.groesseGewichtStore.saveGroesseGewicht({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungGroesseGewichtSaveJson: payload,
      });
    }
  }
}
