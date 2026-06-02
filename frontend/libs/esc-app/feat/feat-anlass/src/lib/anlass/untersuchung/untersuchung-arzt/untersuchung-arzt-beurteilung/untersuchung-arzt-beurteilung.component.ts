import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormArray, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  Aussprache,
  BallFangen,
  BallWerfen,
  Blutdruck,
  FigurNachmalen,
  Hampelmann,
  Haut,
  Herzauskultation,
  Kognition,
  Lesefit,
  NameSchreiben,
  RequirementSetting,
  Stifthaltung,
  Verhalten,
  Verhaltensfragebogen,
  ZahlenNachsprechen,
  Zeichnen,
  ZeichnenMensch,
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
import {hasSomeNotNullishValueIgnoringVersion} from '../../untersuchung.util';
import {UntersuchungArztStore} from '../untersuchung-arzt.store';
import {
  BewegungsapparatSubFormType,
  EinverstaendniserklaerungSubFormType,
  FingeroppositionSubFormType,
  MundAndZaehneSubFormType,
  SprachfaehigkeitMultiValueSubFormType,
  UntersuchungArztBeurteilungForm,
} from './untersuchung-arzt-beurteilung.form';

@Component({
  selector: 'feat-untersuchung-arzt-beurteilung',
  imports: [
    FormLayoutComponent,
    KachelControlComponent,
    KachelFormSectionComponent,
    RadioDirective,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormControlComponent,
    TextareaDirective,
    CheckboxDirective,
    TextInputDirective,
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

  canEdit = computed(
    () =>
      this.benutzerStore.createHasRequiredRoleSig(hasAerztlicheUntersuchungEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  form = new UntersuchungArztBeurteilungForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: this.destroyRef,
    anlassStore: this.anlassStore,
  });

  // single selection items, multiselects are created in form
  verhaltenItems = Object.values(Verhalten);
  blutdruckItems = Object.values(Blutdruck);
  herzauskultationItems = Object.values(Herzauskultation);
  hautItems = Object.values(Haut);
  hampelmannItems = Object.values(Hampelmann);
  ballFangenItems = Object.values(BallFangen);
  ballWerfenItems = Object.values(BallWerfen);
  nameSchreibenItems = Object.values(NameSchreiben);
  figurNachmalenItems = Object.values(FigurNachmalen);
  stifthaltungItems = Object.values(Stifthaltung);
  ausspracheItems = Object.values(Aussprache);
  lesefitItems = Object.values(Lesefit);
  zahlenNachsprechenItems = Object.values(ZahlenNachsprechen);
  zeichnenItems = Object.values(Zeichnen);
  zeichnenMenschItems = Object.values(ZeichnenMensch);
  kognitionItems = Object.values(Kognition);
  verhaltensfragebogenItems = Object.values(Verhaltensfragebogen);

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
        const hasAnyValue = hasSomeNotNullishValueIgnoringVersion(defaultArzt);
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
    this.form.resetForm(arztUntersuchung?.data);
  }

  loadDefault(): void {
    this.store.applyDefaultUntersuchungArzt();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      this.store.saveUntersuchungArzt({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        untersuchungArztBeurteilungSaveJson: payload,
      });
    }
  }

  getEinverstaendniserklaerungArray(): FormArray<EinverstaendniserklaerungSubFormType> {
    return this.form.group.controls.einverstaendniserklaerungArray;
  }

  getSprachfaehigkeitMultiValueArray(): FormArray<SprachfaehigkeitMultiValueSubFormType> {
    return this.form.group.controls.sprachfaehigkeitMultiValueArray;
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
