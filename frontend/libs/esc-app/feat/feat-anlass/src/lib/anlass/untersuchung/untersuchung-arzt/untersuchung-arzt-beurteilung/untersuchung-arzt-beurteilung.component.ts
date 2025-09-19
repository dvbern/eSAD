import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormArray, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  Blutdruck,
  Einbeinstand,
  Haut,
  Herzauskultation,
  Motorik,
  RequirementSetting,
  Verhalten,
} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  RadioDirective,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasAerztlicheUntersuchungEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungArztStore} from '../untersuchung-arzt.store';
import {
  BewegungsapparatSubFormType,
  FingeroppositionSubFormType,
  MundAndZaehneSubFormType,
  SprachfaehigkeitSubFormType,
  UntersuchungArztBeurteilungForm,
} from './untersuchung-arzt-beurteilung.form';

@Component({
  selector: 'feat-untersuchung-arzt-beurteilung',
  imports: [
    CommonModule, FormLayoutComponent, KachelControlComponent, KachelFormSectionComponent, RadioDirective,
    ReactiveFormsModule, SaveCancelButtonsComponent, TranslocoPipe, FormControlComponent, TextareaDirective,
    CheckboxDirective, TextInputDirective,
  ],
  templateUrl: './untersuchung-arzt-beurteilung.component.html',
  styleUrl: './untersuchung-arzt-beurteilung.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungArztBeurteilungComponent implements DirtyMarker {

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungArztStore);
  readonly destroyRef = inject(DestroyRef);
  readonly benutzerStore = inject(BenutzerStore);

  protected readonly RequirementSetting = RequirementSetting;

  canEdit = computed(() => this.benutzerStore.createHasRequiredRoleSig(hasAerztlicheUntersuchungEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  form = new UntersuchungArztBeurteilungForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: this.destroyRef,
  });

  // single selection items, multiselects are created in form
  verhaltenItems = Object.values(Verhalten);
  blutdruckItems = Object.values(Blutdruck);
  herzauskultationItems = Object.values(Herzauskultation);
  hautItems = Object.values(Haut);
  motorikItems = Object.values(Motorik);
  einbeinstandItems = Object.values(Einbeinstand);

  constructor() {
    effect(() => {
      const untersuchung = this.store.untersuchungArztBeurteilung();
      this.form.resetForm(untersuchung?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig);
        });
      }
      this.form.setDisabled(!this.canEdit());
    });
    effect(() => {
      // Wenn die Default-Values zu applyen markiert sind, dann das Formular mit den neuen Werten laden
      const defaultArzt = this.store.defaultArztToApply();
      if (isNotNullish(defaultArzt)) {
        const hasAnyValue = Object.values(defaultArzt).some(isNotNullish);
        if (hasAnyValue) {
          this.form.resetForm(defaultArzt);
          this.form.group.markAsDirty();
        }
        this.store.resetApplyDefaultUntersuchungArzt();
        this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      }
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const arztUntersuchung = this.store.untersuchungArztBeurteilung();
    if (arztUntersuchung) {
      this.form.resetForm(arztUntersuchung.data);
    }
  }

  loadDefault(): void {
    this.store.applyDefaultUntersuchungArzt();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      this.store.saveUntersuchungArzt({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungArztBeurteilungSaveJson: payload,
      });
    }
  }

  getSprachfaehigkeitArray(): FormArray<SprachfaehigkeitSubFormType> {
    return this.form.group.controls.sprachfaehigkeitArray;
  }

  getBewegungsapparatArray(): FormArray<BewegungsapparatSubFormType> {
    return this.form.group.controls.bewegungsapparatArray;
  }

  getFingeroppositionArray(): FormArray<FingeroppositionSubFormType> {
    return this.form.group.controls.fingeroppositionArray;
  }

  getMundAndZaehneArray(): FormArray<MundAndZaehneSubFormType> {
    return this.form.group.controls.mundAndZaehneArray;
  }
}
