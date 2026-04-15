import {DatePipe} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  model,
} from '@angular/core';
import {FormArray, FormControl, FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  AnlassConfigJson,
  BenutzerOptionJson,
  EntityAktivStatus,
  ImpfStatus,
  ImpfstoffJson,
  UntersuchungImpfungInternSaveJson,
} from '@dvb/data-access-backend';
import {
  BenutzerOptionsStore,
  ImpfstoffChargeOption,
  ImpfstoffeChargenStore,
  ModalDialogComponent,
  TenantSettingsStore,
  TimetravelStore
} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormGroupComponent,
  FormLayoutComponent,
  NgSelectDirective,
  RadioDirective,
  SaveCancelButtonsComponent,
  SingleCheckboxWrapperComponent,
  SingleRadioWrapperComponent,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {compareFn, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {ImmunitaetKrankheitSubFormType, UntersuchungImpfungenInternForm} from './untersuchung-impfungen-intern.form';

export type ImpfungPopupMode = 'create' | 'edit' | 'verabreicht' | 'nicht-verabreicht';

@Component({
  selector: 'feat-anlass-planung-einladungen-abmelden-modal',
  templateUrl: './untersuchung-impfungen-intern.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ReactiveFormsModule,
    FormControlComponent,
    FormLayoutComponent,
    ModalDialogComponent,
    SaveCancelButtonsComponent,
    DatePipe,
    TextInputDirective,
    FormGroupComponent,
    SingleRadioWrapperComponent,
    RadioDirective,
    TextareaDirective,
    CheckboxDirective,
    SingleCheckboxWrapperComponent
  ]
})
export class UntersuchungImpfungenInternComponent {

  protected readonly activeModal = inject(NgbActiveModal);
  protected readonly translationService = inject(TranslocoService);

  protected readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  protected readonly impfstoffeChargenStore = inject(ImpfstoffeChargenStore);
  protected readonly timetravelStore = inject(TimetravelStore);
  protected readonly tenantSettingStore = inject(TenantSettingsStore);

  protected readonly benutzerOptions = this.benutzerOptionsStore.entities();
  protected readonly impfStatusOptions = Object.values(ImpfStatus);

  protected readonly compareFnImpfstoff = compareFn<ImpfstoffJson>;
  protected readonly compareFnBenutzer = compareFn<BenutzerOptionJson>;

  protected readonly DetailMode = DetailMode;
  protected readonly isNullish = isNullish;
  protected readonly requireValue = requireValue;

  impfung = model<UntersuchungImpfungInternSaveJson>();
  inputMode = model.required<ImpfungPopupMode>();
  currentUser = model.required<BenutzerOptionJson>();
  anlassConfig = model.required<AnlassConfigJson>();

  readonly impfstoffOptions = computed(() => this.impfstoffeChargenStore.entities()
    .filter(impfstoff => impfstoff.data.impfstoffAktivStatus === EntityAktivStatus.AKTIV));

  compareChargeFn(item: ImpfstoffChargeOption, selected: ImpfstoffChargeOption): boolean {
    return item.label === selected.label;
  }

  form = new UntersuchungImpfungenInternForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    impfstoffChargenStore: this.impfstoffeChargenStore,
    timetravelStore: this.timetravelStore,
  });

  constructor() {
    effect(() => {
      const data = this.impfung();
      this.form.resetForm(data, this.anlassConfig(), this.inputMode(), this.currentUser(), this.timetravelStore);
    });
  }

  getTitle(): string {
    switch (this.inputMode()) {
      case 'create':
        return this.translationService.translate('impfungintern.modal.create.titel');
      case 'edit':
        return this.translationService.translate('impfungintern.modal.edit.titel');
      case 'verabreicht':
        return this.translationService.translate('impfungintern.modal.verabreicht.titel');
      case 'nicht-verabreicht':
        return this.translationService.translate('impfungintern.modal.nichtVerabreicht.titel');
    }
  }

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }

  async speichern(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }

  isInputModeOneOf(...modes: ImpfungPopupMode[]): boolean {
    return modes.includes(this.inputMode());
  }

  getImmunitaetKrankheitFormArray(): FormArray<ImmunitaetKrankheitSubFormType> {
    return this.form.getImmunitaetKrankheitFormArray();
  }

  getImmunitaetKrankheitFormControl(index: number, attribute: string): FormControl {
    const control = this.getImmunitaetKrankheitFormArray().at(index).controls[attribute];
    return control as FormControl;
  }
}
