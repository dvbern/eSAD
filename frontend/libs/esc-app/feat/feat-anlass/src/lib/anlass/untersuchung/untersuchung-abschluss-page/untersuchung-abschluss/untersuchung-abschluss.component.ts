import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from "@angular/core";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent
} from "@dvb/ui-components";
import {TranslocoPipe} from "@jsverse/transloco";
import {
  BenutzerStore,
  createSingleValueEnhancedForm,
  TenantSettingsStore,
  ValueEnhancedComponent
} from "@dvb/feat-shared";
import {DirtyMarker} from "@dvb/util-angular";
import {UntersuchungStore} from "../../untersuchung.store";
import {AbschlussResultat, RequirementSetting} from "@dvb/data-access-backend";
import {UntersuchungAbschlussStore} from "./untersuchung-abschluss.store";
import {UntersuchungAbschlussForm} from "./untersuchung-abschluss.form";
import {hasUntersuchungAbschlussEditBerechtigung, requireValue} from "@dvb/util-shared";
import {AnlassStore} from "../../../anlass.store";

@Component({
  selector: 'feat-untersuchung-abschluss',
  imports: [
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormLayoutComponent,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    ValueEnhancedComponent,
    HeadingDirective,
  ],
  templateUrl: './untersuchung-abschluss.component.html',
  styleUrl: './untersuchung-abschluss.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungAbschlussComponent implements DirtyMarker {
  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly store = inject(UntersuchungAbschlussStore);
  readonly #benutzerStore = inject(BenutzerStore);

  abschlussResultatItems = Object.values(AbschlussResultat);

  canEditAbschluss = computed(() => this.#benutzerStore.createHasRequiredRoleSig(
      hasUntersuchungAbschlussEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  form = new UntersuchungAbschlussForm({
    fb: inject(NonNullableFormBuilder),
    ueberweisungValueEnhancedForm: createSingleValueEnhancedForm(this.abschlussResultatItems),
    aerztlicheValidierungValueEnhancedForm: createSingleValueEnhancedForm(this.abschlussResultatItems),
    beratungValueEnhancedForm: createSingleValueEnhancedForm(this.abschlussResultatItems),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const untersuchungAbschluss = this.store.untersuchungAbschluss();
      this.form.resetForm(untersuchungAbschluss?.abschlussData);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      const nichtGeprueftGrundEnabled = this.tenantSettingsStore.nichtGeprueftGrundEnabled();
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, !!nichtGeprueftGrundEnabled);
        });
      }
      this.form.setDisabled(!this.canEditAbschluss());
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const untersuchungAbschluss = this.store.untersuchungAbschluss();
    this.form.resetForm(untersuchungAbschluss?.abschlussData);
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const payload = this.form.valueOnValid();
      this.store.saveUntersuchungAbschluss({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungAbschlussSaveJson: payload,
      });
    }
  }
}
