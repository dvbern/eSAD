import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  Immunsuppression,
  ImpfausweisGeprueft,
  ImpfempfehlungMinimal,
  ImpfStatusMinimal,
  ImpfungDurchgefuehrtMinimal,
  RequirementSetting,
} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  createSingleValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent,
} from '@dvb/feat-shared';
import {
  ButtonsRightContainerComponent,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  SaveCancelButtonsComponent,
  TextInputDirective,
  TitleComponent,
} from '@dvb/ui-components';
import { DirtyMarker } from '@dvb/util-angular';
import { hasImpfungenEditBerechtigung, requireValue } from '@dvb/util-shared';
import { TranslocoPipe } from '@jsverse/transloco';
import { toEinladungBackendEntityIdWithClass } from '@dvb/data-access-types';
import { AnlassStore } from '../../../anlass.store';
import { UntersuchungStore } from '../../untersuchung.store';
import { UntersuchungImpfungenStore } from '../untersuchung-impfungen.store';
import { UntersuchungImpfungenDataMinimalForm } from './untersuchung-impfungen-data-minimal.form';

@Component({
  selector: 'feat-untersuchung-impfungen-data-minimal',
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    TranslocoPipe,
    SaveCancelButtonsComponent,
    HeadingDirective,
    ButtonsRightContainerComponent,
    FormsModule,
    ValueEnhancedComponent,
    FormControlComponent,
    TitleComponent,
    TextInputDirective,
  ],
  templateUrl: './untersuchung-impfungen-data-minimal.component.html',
  styleUrl: './untersuchung-impfungen-data-minimal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungImpfungenDataMinimalComponent implements DirtyMarker {
  readonly impfungenStore = inject(UntersuchungImpfungenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  impfausweisGeprueftValues = Object.values(ImpfausweisGeprueft);
  immunsuppressionValues = Object.values(Immunsuppression);
  impfStatusMinimalValues = Object.values(ImpfStatusMinimal);
  impfempfehlungMinimalValues = Object.values(ImpfempfehlungMinimal);
  impfungDurchgefuehrtMinimalValues = Object.values(ImpfungDurchgefuehrtMinimal);

  protected readonly RequirementSetting = RequirementSetting;

  canEdit = computed(
    () =>
      this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  readonly form = new UntersuchungImpfungenDataMinimalForm({
    fb: inject(NonNullableFormBuilder),
    impfausweisGeprueftForm: createSingleValueEnhancedForm(this.impfausweisGeprueftValues),
    immunsuppressionForm: createSingleValueEnhancedForm(this.immunsuppressionValues),
    masernImpfstatusForm: createSingleValueEnhancedForm(this.impfStatusMinimalValues),
    hpvImpfstatusForm: createSingleValueEnhancedForm(this.impfStatusMinimalValues),
    impfempfehlungForm: createSingleValueEnhancedForm(this.impfempfehlungMinimalValues),
    impfungDurchgefuehrtForm: createSingleValueEnhancedForm(this.impfungDurchgefuehrtMinimalValues),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const dataMinimal = this.impfungenStore.dataMinimal();
      this.form.resetForm(dataMinimal?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(
            anlassConfig,
            this.tenantSettingsStore.nichtGeprueftGrundEnabled() ?? false,
          );
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.impfungenStore.saveDataMinimalLoader.isLoading();
  }

  abbrechen(): void {
    const dataMinimal = this.impfungenStore.dataMinimal();
    this.form.resetForm(dataMinimal?.data);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      this.impfungenStore.saveDataMinimal({
        anlassId: requireValue(this.anlassStore.selectedEntity()).id,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungImpfungenDataMinimalSaveJson: payload,
      });
    }
  }
}
