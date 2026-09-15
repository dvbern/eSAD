import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from "@angular/core";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {
  CardInCardComponent,
  FormControlComponent,
  FormLayoutComponent,
  IconComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TextInputDirective,
  TitleComponent
} from "@dvb/ui-components";
import {TranslocoPipe} from "@jsverse/transloco";
import {DirtyMarker} from "@dvb/util-angular";
import {UntersuchungStore} from "../untersuchung.store";
import {
  Feinmotorik,
  FreiesGehen,
  GleichgewichtEinbeinstand,
  Grafomotorik,
  GrobmotorikEinbeinhuepfen,
  OhneStuetzradVelofahren,
  RequirementSetting,
} from "@dvb/data-access-backend";
import {requireValue} from "@dvb/util-shared";
import {UntersuchungSchulrelevanteBeeintraechtigungStore} from "./untersuchung-schulrelevante-beeintraechtigung.store";
import {UntersuchungSchulrelevanteBeeintraechtigungForm} from "./untersuchung-schulrelevante-beeintraechtigung.form";
import {AnlassStore} from "../../anlass.store";
import {createSingleValueEnhancedForm, TenantSettingsStore, ValueEnhancedComponent} from "@dvb/feat-shared";
import {setupApplyDefaultValuesEffect} from "../default-values.util";

@Component({
  selector: 'feat-untersuchung-schulrelevante-beeintraechtigung',
  imports: [
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormLayoutComponent,
    FormControlComponent,
    TextareaDirective,
    TitleComponent,
    ValueEnhancedComponent,
    TextInputDirective,
    CardInCardComponent,
    IconComponent,
  ],
  templateUrl: './untersuchung-schulrelevante-beeintraechtigung.component.html',
  styleUrl: './untersuchung-schulrelevante-beeintraechtigung.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSchulrelevanteBeeintraechtigungComponent implements DirtyMarker {
  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungSchulrelevanteBeeintraechtigungStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  freiesGehen = Object.values(FreiesGehen);
  ohneStuetzradVelofahren = Object.values(OhneStuetzradVelofahren);
  grafomotorik = Object.values(Grafomotorik);
  feinmotorik = Object.values(Feinmotorik);
  grobmotorikEinbeinhuepfen = Object.values(GrobmotorikEinbeinhuepfen);
  gleichgewichtEinbeinstand = Object.values(GleichgewichtEinbeinstand);

  form = new UntersuchungSchulrelevanteBeeintraechtigungForm({
    fb: inject(NonNullableFormBuilder),
    freiesGehenValueEnhancedForm: createSingleValueEnhancedForm(this.freiesGehen),
    ohneStuetzradVelofahrenValueEnhancedForm: createSingleValueEnhancedForm(this.ohneStuetzradVelofahren),
    grafomotorikValueEnhancedForm: createSingleValueEnhancedForm(this.grafomotorik),
    feinmotorikValueEnhancedForm: createSingleValueEnhancedForm(this.feinmotorik),
    grobmotorikEinbeinhuepfenValueEnhancedForm: createSingleValueEnhancedForm(this.grobmotorikEinbeinhuepfen),
    gleichgewichtEinbeinstandValueEnhancedForm: createSingleValueEnhancedForm(this.gleichgewichtEinbeinstand),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const untersuchungSchulrelevanteBeeintraechtigung = this.store.untersuchungSchulrelevanteBeeintraechtigung();
      this.form.resetForm(untersuchungSchulrelevanteBeeintraechtigung?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      const nichtGeprueftGrundEnabled = this.tenantSettingsStore.nichtGeprueftGrundEnabled();
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig, !!nichtGeprueftGrundEnabled);
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
    const untersuchungSchulrelevanteBeeintraechtigung = this.store.untersuchungSchulrelevanteBeeintraechtigung();
    this.form.resetForm(untersuchungSchulrelevanteBeeintraechtigung?.data);
  }

  loadDefaults(): void {
    this.store.applyDefault();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const payload = this.form.valueOnValid();
      this.store.saveUntersuchungSchulrelevanteBeeintraechtigung({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungSchulrelevanteBeeintraechtigungSaveJson: payload,
      });
    }
  }
}
