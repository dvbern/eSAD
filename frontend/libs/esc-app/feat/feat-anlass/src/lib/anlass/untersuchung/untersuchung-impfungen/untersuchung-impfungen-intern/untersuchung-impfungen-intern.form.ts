import {DestroyRef, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  BenutzerOptionJson,
  ImpfStatus,
  ImpfstoffChargeJson,
  ImpfstoffJson,
  Krankheit,
  RequirementSetting,
  UntersuchungImpfungInternSaveJson,
  UntersuchungImpfungKrankheitImmunitaetJson,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {globalConstants, ImpfstoffeChargenStore, TimetravelStore} from '@dvb/feat-shared';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {ensureProps, ensureType, requireValue, today, toISODateString} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {ImpfungPopupMode} from './untersuchung-impfungen-intern.component';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  impfstoffChargenStore: ImpfstoffeChargenStore,
  timetravelStore: TimetravelStore
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    impfstoff: fb.control<ImpfstoffJson | undefined>(undefined, [Validators.required]),
    charge: fb.control<ImpfstoffChargeJson | undefined>(undefined),
    datumImpfung: fb.control<ISODateString | undefined>(undefined),
    userDurchgefuehrt: fb.control<BenutzerOptionJson | undefined>(undefined),
    impfStatus: fb.control<ImpfStatus | undefined>(undefined),
    grundNichtDurchfuehrung: fb.control<string | undefined>(undefined),
    krankheitsImmunitaeten: fb.array<ImmunitaetKrankheitSubFormType>([]),
    version: fb.control<number | undefined>({value: undefined, disabled: true}),
  });
}

export type ImmunitaetKrankheitSubFormType = ReturnType<typeof makeSubform>;

function makeSubform(config: Config, krankheit: Krankheit): FormGroup {
  const fb = config.fb;
  return fb.group({
    krankheit: fb.control<Krankheit>({value: krankheit, disabled: true}),
    immunitaetGenuegend: fb.control<boolean>(false),
  });
}

export class UntersuchungImpfungenInternForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungImpfungInternSaveJson> {

  public readonly chargeOptions = signal<ImpfstoffChargeJson[]>([]);

  constructor(protected config: Config) {
    super(() => makeForm(config));

    this.group.controls.impfstoff.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: value => {
          // ChargeOptions aufgrund Impfstoff anpassen
          this.group.controls.charge.reset();
          this.group.controls.krankheitsImmunitaeten.clear();
          this.chargeOptions.set(config.impfstoffChargenStore.activeChargenForImpfstoff(value?.id));
          // Die Immunitaets-SubForms pro Krankheit des Impfstoffs erstellen
          if (value) {
            for (const krankheit of value.data.krankheiten) {
              const subForm: ImmunitaetKrankheitSubFormType = makeSubform(config, krankheit);
              const immunitaetenFormArray = this.group.get('krankheitsImmunitaeten') as FormArray;
              immunitaetenFormArray.push(subForm);
            }
          }
        },
        error: console.error,
      });

    // Eine Eingabe aendert den required-Status von anderen Feldern
    this.group.controls.impfStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: _ => {
          this.group.controls.datumImpfung.updateValueAndValidity();
          this.group.controls.charge.updateValueAndValidity();
          this.group.controls.grundNichtDurchfuehrung.updateValueAndValidity();
          this.group.controls.userDurchgefuehrt.updateValueAndValidity();
        },
        error: console.error,
      });
  }

  resetForm(
    impfung: UntersuchungImpfungInternSaveJson | undefined,
    anlassConfig: AnlassConfigJson,
    inputMode: ImpfungPopupMode,
    currentUser: BenutzerOptionJson,
    timetravelStore: TimetravelStore,
  ): void {
    super.reset(impfung, DetailMode.EDIT);
    if (impfung) {
      for (const immunitaetKrankheit of impfung.krankheitenImmunitaeten) {
        const immunitaetForm = this.getImmunitaetKrankheitFormControl(immunitaetKrankheit.krankheit);
        if (immunitaetForm) {
          immunitaetForm.patchValue(immunitaetKrankheit);
        }
      }
  }
    // Validationen anhand der AnlassConfig setzen
    if (anlassConfig.impfungenListe === RequirementSetting.REQUIRED) {
      this.group.controls.datumImpfung.addValidators(AppValidators.nonFutureDates(
        () => this.group.controls.impfStatus.value !== ImpfStatus.GEPLANT,
        timetravelStore.currentDate()));
      this.group.controls.datumImpfung.addValidators(AppValidators.requiredIfValidator(() => this.group.controls.impfStatus.value
        === ImpfStatus.DURCHGEFUEHRT));
      this.group.controls.charge.addValidators(AppValidators.requiredIfValidator(() => this.group.controls.impfStatus.value
        === ImpfStatus.DURCHGEFUEHRT));
      this.group.controls.userDurchgefuehrt.addValidators(AppValidators.requiredIfValidator(() => this.group.controls.impfStatus.value
        === ImpfStatus.DURCHGEFUEHRT));
      this.group.controls.grundNichtDurchfuehrung.addValidators(AppValidators.requiredIfValidator(() => this.group.controls.impfStatus.value
        === ImpfStatus.NICHT_DURCHGEFUEHRT));

      this.group.controls.datumImpfung.updateValueAndValidity();
      this.group.controls.charge.updateValueAndValidity();
      this.group.controls.grundNichtDurchfuehrung.updateValueAndValidity();
      this.group.controls.userDurchgefuehrt.updateValueAndValidity();
    }

    // Defaultwerte aufgrund InputMode setzen
    if (inputMode === 'verabreicht') {
      this.group.controls.impfStatus.setValue(ImpfStatus.DURCHGEFUEHRT);
      this.group.controls.datumImpfung.setValue(toISODateString(today()));
      this.group.controls.userDurchgefuehrt.setValue(currentUser)
    } else if (inputMode === 'nicht-verabreicht') {
      this.group.controls.impfStatus.setValue(ImpfStatus.NICHT_DURCHGEFUEHRT);
      this.group.controls.datumImpfung.setValue(toISODateString(today()));
    }
  }

  valueOnValid(): UntersuchungImpfungInternSaveJson {
    const value = requireValue(this.group.getRawValue());
    const result: UntersuchungImpfungKrankheitImmunitaetJson[] = [];
    Object.keys(this.group.controls.krankheitsImmunitaeten.controls).forEach(key => {
      // @ts-expect-error krankheit is not allowed as an index but that should be ok
      const krankheitGroup: ImmunitaetKrankheitSubFormType = this.group.controls.krankheitsImmunitaeten.controls[key] as ReturnType<typeof makeSubform>;
      const rawValue = krankheitGroup.getRawValue();
      const item = ensureProps<UntersuchungImpfungKrankheitImmunitaetJson>({
        krankheit: rawValue.krankheit,
        immunitaetGenuegend: rawValue.immunitaetGenuegend,
      });
      result.push(item);
    });
    return ensureType<UntersuchungImpfungInternSaveJson>({
      impfstoff: requireValue(value.impfstoff),
      charge: value.charge,
      datumImpfung: value.datumImpfung,
      userDurchgefuehrt: value.userDurchgefuehrt,
      impfStatus: value.impfStatus ?? ImpfStatus.GEPLANT,
      grundNichtDurchfuehrung: value.grundNichtDurchfuehrung,
      krankheitenImmunitaeten: result,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  getImmunitaetKrankheitFormArray(): FormArray<ImmunitaetKrankheitSubFormType> {
    return this.group.controls.krankheitsImmunitaeten;
  }

  getImmunitaetKrankheitFormControl(krankheit: Krankheit): FormControl | undefined {
    const array: FormArray<ImmunitaetKrankheitSubFormType> = this.getImmunitaetKrankheitFormArray();
    const anzahl: number = array.length;
    for (let i = 0; i < anzahl; i++) {
      if (array.at(i).controls['krankheit'].value === krankheit) {
        return array.at(i) as unknown as FormControl;
      }
    }
    return undefined;
  }
}
