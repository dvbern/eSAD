import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from '@angular/core';
import {
  CardInCardComponent,
  FormControlComponent,
  FormLayoutComponent,
  IconComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent
} from '@dvb/ui-components';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {TranslocoPipe} from '@jsverse/transloco';
import {DirtyMarker} from '@dvb/util-angular';
import {UntersuchungStore} from '../untersuchung.store';
import {requireValue} from '@dvb/util-shared';
import {UntersuchungBewegungsapparatStore} from './untersuchung-bewegungsapparat.store';
import {
  Haltungsschwaeche,
  RequirementSetting,
  Skoliose,
  VerkuerzungHamstringmuskulatur
} from '@dvb/data-access-backend';
import {UntersuchungBewegungsapparatForm} from './untersuchung-bewegungsapparat.form';
import {AnlassStore} from '../../anlass.store';
import {createSingleValueEnhancedForm, TenantSettingsStore, ValueEnhancedComponent} from "@dvb/feat-shared";
import {setupApplyDefaultValuesEffect} from "../default-values.util";

@Component({
  selector: 'feat-untersuchung-bewegungsapparat',
  imports: [
    ReactiveFormsModule,
    TextareaDirective,
    TranslocoPipe,
    SaveCancelButtonsComponent,
    FormControlComponent,
    TitleComponent,
    FormLayoutComponent,
    ValueEnhancedComponent,
    CardInCardComponent,
    IconComponent,
  ],
  templateUrl: './untersuchung-bewegungsapparat.component.html',
  styleUrl: './untersuchung-bewegungsapparat.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungBewegungsapparatComponent implements DirtyMarker {
  readonly store = inject(UntersuchungBewegungsapparatStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  readonly RequirementSetting = RequirementSetting;

  skoliose = Object.values(Skoliose);
  haltungsschwachen = Object.values(Haltungsschwaeche);
  verkuerzungHamstringmuskulatur = Object.values(VerkuerzungHamstringmuskulatur);

  public form = new UntersuchungBewegungsapparatForm({
    fb: inject(NonNullableFormBuilder),
    skolioseValueEnhancedForm: createSingleValueEnhancedForm(this.skoliose),
    haltungsschwaecheValueEnhancedForm: createSingleValueEnhancedForm(this.haltungsschwachen),
    verkuerzungHamstringmuskulaturValueEnhancedForm: createSingleValueEnhancedForm(this.verkuerzungHamstringmuskulatur),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const bewegungsapparat = this.store.bewegungsapparat();
      this.form.resetForm(bewegungsapparat?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      const nichtGeprueftGrundEnabled = this.tenantSettingsStore.nichtGeprueftGrundEnabled();
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidators(anlassConfig, !!nichtGeprueftGrundEnabled);
        });
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
    });
    setupApplyDefaultValuesEffect({
      defaultValuesToApply: this.store.defaultToApply,
      form: this.form,
      resetApplyDefault: () => this.store.resetApplyDefault(),
      canEdit: () => this.anlassStore.canUserEditAnlassInCurrentState(),
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    this.form.resetForm(this.store.bewegungsapparat()?.data ?? undefined);
  }

  loadDefaults(): void {
    this.store.applyDefault();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.getWrappedEinladungId());
      this.store.saveBewegungsapparat({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungBewegungsapparatSaveJson: payload,
      });
    }
  }
}
