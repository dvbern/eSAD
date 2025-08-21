import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from '@angular/core';
import {FormArray, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  BrilleLinsenResultat,
  FarbsehenResultat,
  InfomaterialJson,
  InformationEltern,
  InspektionKonvergenzResultat,
  MotilitaetResultat,
  RequirementSetting,
  StereotestResultat,
  StrabismusAuftretenResultat,
  StrabismusAuspraegungResultat,
  VerwendetTafelResultat,
} from '@dvb/data-access-backend';
import {InfomaterialStore, TranslatedStringPipe} from '@dvb/feat-shared';
import {
  CardInCardComponent,
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  IconComponent,
  KachelControlComponent,
  KachelFormSectionComponent,
  NgSelectDirective,
  RadioDirective,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {compareFn, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../anlass.store';
import {UntersuchungStore} from '../untersuchung.store';
import {DistanzSehenSubFormType, StrabismusSubFormType, UntersuchungSehenForm} from './untersuchung-sehen.form';
import {UntersuchungSehenStore} from './untersuchung-sehen.store';

@Component({
  selector: 'feat-untersuchung-sehen',
  imports: [
    CommonModule, ReactiveFormsModule, SaveCancelButtonsComponent,
    TranslocoPipe, FormLayoutComponent, KachelFormSectionComponent,
    KachelControlComponent, RadioDirective, FormControlComponent, CheckboxDirective,
    TextareaDirective, TitleComponent, NgOptionComponent, NgSelectComponent, NgSelectDirective, TranslatedStringPipe,
    CardInCardComponent, IconComponent,
  ],
  templateUrl: './untersuchung-sehen.component.html',
  styleUrl: './untersuchung-sehen.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSehenComponent implements DirtyMarker {

  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungSehenStore);
  readonly infomaterialStore = inject(InfomaterialStore);

  form = new UntersuchungSehenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  brilleLinsenItems = Object.values(BrilleLinsenResultat);
  stereotestItems = Object.values(StereotestResultat);
  farbsehenItems = Object.values(FarbsehenResultat);
  verwendeteTafelItems = Object.values(VerwendetTafelResultat);
  motilitaetItems = Object.values(MotilitaetResultat);
  strabismusAuftretenItems = Object.values(StrabismusAuftretenResultat);
  strabismusAuspraegungItems = Object.values(StrabismusAuspraegungResultat);
  inspektionKonvergenzItems = Object.values(InspektionKonvergenzResultat);
  informationEltern = Object.values(InformationEltern);
  infomaterialOptions: InfomaterialJson[] | undefined;
  visusOptions = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0];

  protected readonly compareInfomaterial = compareFn<InfomaterialJson>;

  constructor() {
    effect(() => {
      const sehen = this.store.sehen();
      this.form.resetForm(sehen?.data);
      const anlassConfig = this.anlassStore.selectedEntity()?.anlassConfig;
      if (anlassConfig) {
        untracked(() => {
          this.form.setValidatorsForAnlassConfig(anlassConfig);
        });
      }
      this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      this.infomaterialOptions =
        this.infomaterialStore.patchInfomaterialOptions(sehen?.data?.massnahmen?.abgegebenesInfomaterial);
    });
    effect(() => {
      // Wenn die Default-Values zu applyen markiert sind, dann das Formular mit den neuen Werten laden
      const defaultSehen = this.store.defaultSehenToApply();
      if (isNotNullish(defaultSehen)) {
        const hasAnyValue = Object.values(defaultSehen).some(isNotNullish);
        if (hasAnyValue) {
          this.form.resetForm(defaultSehen);
          this.form.group.markAsDirty();
        }
        this.store.resetApplyDefaultSehen();
        this.form.setDisabled(!this.anlassStore.canUserEditAnlassInCurrentState());
      }
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.store.saveLoader.isLoading();
  }

  abbrechen(): void {
    const sehen = this.store.sehen();
    if (sehen) {
      this.form.resetForm(sehen.data);
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
      const einladungId = requireValue(this.untersuchungStore.currentTab()?.einladungID);
      const payload = this.form.valueOnValid(
        this.anlassStore.selectedEntity()?.anlassConfig?.sehenMassnahmen !== RequirementSetting.NOT_VISIBLE);
      this.store.saveSehen({
        anlassId: anlassId,
        einladungId: einladungId,
        untersuchungSehenSaveJson: payload,
      });
    }
  }

  loadDefault(): void {
    this.store.applyDefaultSehen();
  }

  getFernvisusFormArray(): FormArray<DistanzSehenSubFormType> {
    return this.form.group.controls.fernvisusArray;
  }

  getNahvisusFormArray(): FormArray<DistanzSehenSubFormType> {
    return this.form.group.controls.nahvisusArray;
  }

  getStrabismusFormArray(): FormArray<StrabismusSubFormType> {
    return this.form.group.controls.strabismusArray;
  }
}
