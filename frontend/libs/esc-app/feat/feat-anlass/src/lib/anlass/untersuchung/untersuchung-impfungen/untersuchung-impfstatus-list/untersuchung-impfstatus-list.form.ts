import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormGroup, NonNullableFormBuilder} from '@angular/forms';
import {
  AnlassConfigJson,
  Krankheit,
  RequirementSetting,
  UntersuchungImpfstatusKrankheitSaveJson,
  UntersuchungImpfstatusListSaveJson,
  UntersuchungImpfungenImpfausweisSaveJson,
} from '@dvb/data-access-backend';
import {ISOYearMonthString} from '@dvb/data-access-types';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, getDateFromISOYearMonthString, getYearMonthFromISODateString, Nullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  krankheiten: Krankheit[],
};

export type UntersuchungImpfstatusSubFormType = ReturnType<typeof makeSubform>;

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    impfentscheide: fb.group({}),
  });
  config.krankheiten.forEach(k => group.controls.impfentscheide.addControl(k, makeSubform(config, k)));
  return group;
}

function makeSubform(config: Config, krankheit: Krankheit): FormGroup {
  const fb = config.fb;
  const group = fb.group({
    krankheit: fb.control<Krankheit>(krankheit),
    anzahlImpfungen: fb.control<number | null>(null, [
      AppValidators.nonNegativNumber(),
      AppValidators.numberOrNumericChars(),
    ]),
    datumLetzteImpfung: fb.control<ISOYearMonthString | null>(null, [AppValidators.minYear(1900)]),
    impfschutzGegeben: fb.control<boolean>(false),
    impfempfehlung: fb.control<boolean>(false),
    impfentscheid: fb.control<boolean>(false),
  });

  // Eine Checkbox wählen soll die anderen abwählen:
  group.controls.impfschutzGegeben.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (value) {
          group.controls.impfempfehlung.setValue(false);
          group.controls.impfentscheid.setValue(false);
        }
      },
      error: console.error,
    });
  group.controls.impfempfehlung.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (value) {
          group.controls.impfschutzGegeben.setValue(false);
          group.controls.impfentscheid.setValue(false);
        }
      },
      error: console.error,
    });
  group.controls.impfentscheid.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: value => {
        if (value) {
          group.controls.impfschutzGegeben.setValue(false);
          group.controls.impfempfehlung.setValue(false);
        }
      },
      error: console.error,
    });

  // Eine Eingabe aendert den required-Status eines anderen Feldes
  group.controls.anzahlImpfungen.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.datumLetzteImpfung.updateValueAndValidity();
      },
      error: console.error,
    });

  group.controls.datumLetzteImpfung.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.anzahlImpfungen.updateValueAndValidity();
      },
      error: console.error,
    });

  return group;
}

export class UntersuchungImpfstatusListForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungImpfstatusListSaveJson
> {
  private readonly MIN_YEAR_FOR_LAST_IMPFUNG = 1900;

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): UntersuchungImpfstatusListSaveJson {
    const newValue: UntersuchungImpfstatusListSaveJson = { impfstatusList: [] };
    Object.keys(this.group.controls.impfentscheide.controls).forEach(krankheit => {
      const krankheitGroup: UntersuchungImpfstatusSubFormType = this.getKrankheitGroup(krankheit as Krankheit);
      const rawValue = krankheitGroup.getRawValue();

      const item = ensureProps<UntersuchungImpfstatusKrankheitSaveJson>({
        krankheit: rawValue.krankheit,
        anzahlImpfungen: rawValue.anzahlImpfungen,
        datumLetzteImpfung: getDateFromISOYearMonthString(rawValue.datumLetzteImpfung),
        impfschutzGegeben: rawValue.impfschutzGegeben,
        impfempfehlung: rawValue.impfempfehlung,
        impfentscheid: rawValue.impfentscheid,
      });
      newValue.impfstatusList.push(item);
    });
    return newValue;
  }

  private getKrankheitGroup(krankheit: Krankheit): ReturnType<typeof makeSubform> {
    // @ts-expect-error krankheit is not allowed as an index but that should be ok
    const krankheitGroup = this.group.controls.impfentscheide.controls[krankheit] as ReturnType<typeof makeSubform>;
    return krankheitGroup;
  }

  resetForm(data: UntersuchungImpfstatusListSaveJson | Nullish): void {
    this.group.reset();
    this.config.krankheiten.forEach(k => {
      const krankheitGroup: UntersuchungImpfstatusSubFormType = this.getKrankheitGroup(k);
      const krankheitData = data?.impfstatusList.find(k2 => k2.krankheit === k);
      const yearMont = getYearMonthFromISODateString(krankheitData?.datumLetzteImpfung);
      const inputToReset = { ...krankheitData, datumLetzteImpfung: yearMont };
      krankheitGroup.reset(inputToReset);
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, impfausweis: UntersuchungImpfungenImpfausweisSaveJson): void {
    Object.keys(this.group.controls.impfentscheide.controls).forEach(krankheit => {
      const krankheitGroup: UntersuchungImpfstatusSubFormType = this.getKrankheitGroup(krankheit as Krankheit);
      // Wenn AnzahlImpfungen gesetzt, ist DatumLetzteImpfung Pflicht, u.U.
      if (config.impfungenUebersicht === RequirementSetting.REQUIRED) {
        krankheitGroup.controls['datumLetzteImpfung'].setValidators([
          AppValidators.minYear(this.MIN_YEAR_FOR_LAST_IMPFUNG),
            AppValidators.requiredIfValidator(() => (
              krankheitGroup.controls['anzahlImpfungen'].value ?? 0) > 0
              && !impfausweis.impfausweisUnvollstaendig
              && !impfausweis.impfausweisNichtVorhanden),
        ]);
        krankheitGroup.controls['anzahlImpfungen'].addValidators(
          AppValidators.requiredIfValidator(() => !!krankheitGroup.controls['datumLetzteImpfung'].value),
        );
      } else {
        krankheitGroup.controls['datumLetzteImpfung'].clearValidators();
        krankheitGroup.controls['datumLetzteImpfung'].setValidators([
          AppValidators.minYear(this.MIN_YEAR_FOR_LAST_IMPFUNG),
        ]);
        krankheitGroup.controls['anzahlImpfungen'].clearValidators();
      }
    });
  }
}
