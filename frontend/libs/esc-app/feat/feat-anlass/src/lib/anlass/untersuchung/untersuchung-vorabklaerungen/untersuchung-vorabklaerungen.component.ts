import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from "@angular/core";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {FormLayoutComponent, SaveCancelButtonsComponent} from "@dvb/ui-components";
import {
  BenutzerStore,
  createSingleValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent
} from "@dvb/feat-shared";
import {DirtyMarker} from "@dvb/util-angular";
import {UntersuchungStore} from "../untersuchung.store";
import {
  ChronischeKrankheiten,
  EinverstaendnisImpfungen,
  FragebogenStatus,
  RequirementSetting
} from "@dvb/data-access-backend";
import {UntersuchungVorabklaerungenStore} from "./untersuchung-vorabklaerungen.store";
import {UntersuchungVorabklaerungenForm} from "./untersuchung-vorabklaerungen.form";
import {hasUntersuchungVorabklaerungenEditBerechtigung, requireValue} from "@dvb/util-shared";
import {AnlassStore} from "../../anlass.store";

@Component({
  selector: 'feat-untersuchung-vorabklaerungen',
  imports: [
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    FormLayoutComponent,
    ValueEnhancedComponent,
  ],
  templateUrl: './untersuchung-vorabklaerungen.component.html',
  styleUrl: './untersuchung-vorabklaerungen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungVorabklaerungenComponent implements DirtyMarker {
  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly store = inject(UntersuchungVorabklaerungenStore);
  readonly #benutzerStore = inject(BenutzerStore);

  fragebogenStatusItems = Object.values(FragebogenStatus);
  einverstaendnisImpfungenItems = Object.values(EinverstaendnisImpfungen);
  chronischeKrankheitenItems = Object.values(ChronischeKrankheiten);

  canEditVorabklaerungen = computed(() => this.#benutzerStore.createHasRequiredRoleSig(hasUntersuchungVorabklaerungenEditBerechtigung())
    && this.anlassStore.canUserEditAnlassInCurrentState());

  form = new UntersuchungVorabklaerungenForm({
    fb: inject(NonNullableFormBuilder),
    fragebogenStatusForm: createSingleValueEnhancedForm(this.fragebogenStatusItems),
    einverstaendnisImpfungenForm: createSingleValueEnhancedForm(this.einverstaendnisImpfungenItems),
    chronischeKrankheitenForm: createSingleValueEnhancedForm(this.chronischeKrankheitenItems),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const untersuchungVorabklaerungen = this.store.untersuchungVorabklaerungen();
      this.form.resetForm(untersuchungVorabklaerungen?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      const nichtGeprueftGrundEnabled = this.tenantSettingsStore.nichtGeprueftGrundEnabled();
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, !!nichtGeprueftGrundEnabled);
        });
      }
      this.form.setDisabled(!this.canEditVorabklaerungen());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const untersuchungVorabklaerungen = this.store.untersuchungVorabklaerungen();
    if (untersuchungVorabklaerungen) {
      this.form.resetForm(untersuchungVorabklaerungen.data);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      const payload = this.form.valueOnValid();
      this.store.saveUntersuchungVorabklaerungen({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungVorabklaerungenSaveJson: payload,
      });
    }
  }
}
