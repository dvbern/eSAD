import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl, FormGroup, NonNullableFormBuilder, ValidationErrors, ValidatorFn} from '@angular/forms';
import {
  AnlassConfigJson,
  ImpfschutzGegeben,
  Krankheit,
  RequirementSetting,
  UntersuchungImpfstatusKrankheitSaveJson,
  UntersuchungImpfstatusListSaveJson,
  UntersuchungImpfungenImpfausweisSaveJson,
} from '@dvb/data-access-backend';
import {ISOYearMonthString} from '@dvb/data-access-types';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {
  ensureProps,
  getDateFromISOYearMonthString,
  getYearMonthFromISODateString,
  isNotNullish,
  Nullish
} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  krankheiten: Krankheit[],
  anlassConfig: AnlassConfigJson | undefined
};

export type UntersuchungImpfstatusSubFormType = ReturnType<typeof makeSubform>;

function makeForm(config: Config) {
  return config.fb.group({
    impfentscheide: config.fb.group({}),
  });
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
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
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

  public isNurUnvollstaendige(): boolean {
    return this.config.anlassConfig?.data.impfungenNurUnvollstaendige !== RequirementSetting.NOT_VISIBLE;
  }

  valueOnValid(): UntersuchungImpfstatusListSaveJson {
    const newValue: UntersuchungImpfstatusListSaveJson = {impfstatusList: []};
    Object.keys(this.group.controls.impfentscheide.controls).forEach(krankheit => {
      const krankheitGroup: UntersuchungImpfstatusSubFormType = this.getKrankheitGroup(krankheit as Krankheit);
      const rawValue = krankheitGroup.getRawValue();

      // Im Frontend haben wir ein boolean, im Backend ein Enum:
      const impfschutzGegebenRaw = this.isNurUnvollstaendige() ? !rawValue.impfschutzGegeben : rawValue.impfschutzGegeben;
      const impfschutzGegeben = impfschutzGegebenRaw ? ImpfschutzGegeben.JA : ImpfschutzGegeben.NEIN;

      if (this.isNurUnvollstaendige()) {
        const item = ensureProps<UntersuchungImpfstatusKrankheitSaveJson>({
          krankheit: rawValue.krankheit,
          anzahlImpfungen: undefined,
          datumLetzteImpfung: undefined,
          impfschutzGegeben: impfschutzGegeben,
          impfempfehlung: undefined,
          impfentscheid: undefined,
          version: rawValue.version ?? globalConstants.entityPrePersistVersion,
        });
        newValue.impfstatusList.push(item);
      } else {
        const item = ensureProps<UntersuchungImpfstatusKrankheitSaveJson>({
          krankheit: rawValue.krankheit,
          anzahlImpfungen: rawValue.anzahlImpfungen,
          datumLetzteImpfung: getDateFromISOYearMonthString(rawValue.datumLetzteImpfung),
          impfschutzGegeben: impfschutzGegeben,
          impfempfehlung: rawValue.impfempfehlung,
          impfentscheid: rawValue.impfentscheid,
          version: rawValue.version ?? globalConstants.entityPrePersistVersion,
        });
        newValue.impfstatusList.push(item);
      }
    });

    return newValue;
  }

  private getKrankheitGroup(krankheit: Krankheit): ReturnType<typeof makeSubform> {
    // @ts-expect-error krankheit is not allowed as an index but that should be ok
    return this.group.controls.impfentscheide.controls[krankheit] as ReturnType<typeof makeSubform>;
  }

  resetForm(data: UntersuchungImpfstatusListSaveJson | Nullish): void {
    this.group.reset();
    this.config.krankheiten.forEach(k => {
      const krankheitGroup: UntersuchungImpfstatusSubFormType = this.getKrankheitGroup(k);
      const krankheitData = data?.impfstatusList.find(k2 => k2.krankheit === k);
      const yearMont = getYearMonthFromISODateString(krankheitData?.datumLetzteImpfung);
      const impfschutzGegeben = this.isNurUnvollstaendige() ? krankheitData?.impfschutzGegeben == ImpfschutzGegeben.NEIN : krankheitData?.impfschutzGegeben == ImpfschutzGegeben.JA;
      const inputToReset = {...krankheitData, datumLetzteImpfung: yearMont, impfschutzGegeben};
      krankheitGroup.reset(inputToReset);
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, impfausweis: UntersuchungImpfungenImpfausweisSaveJson): void {
    Object.keys(this.group.controls.impfentscheide.controls).forEach(krankheit => {
      const krankheitGroup: UntersuchungImpfstatusSubFormType = this.getKrankheitGroup(krankheit as Krankheit);
      // Wenn AnzahlImpfungen gesetzt, ist DatumLetzteImpfung Pflicht, u.U.
      if (config.data.impfungenUebersicht === RequirementSetting.REQUIRED) {
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

    // Bei ImpfausweisVollstaendig = NEIN (nur unvollstaendige) muss mindestens eine Krankheit ausgewaehlt sein.
    if (this.isNurUnvollstaendige()) {
      this.group.controls.impfentscheide.setValidators(this.atLeastOneKrankheitSelectedValidator());
    } else {
      this.group.controls.impfentscheide.clearValidators();
    }
    this.group.controls.impfentscheide.updateValueAndValidity();
  }

  /**
   * Validiert, dass im Modus "nur unvollstaendige" mindestens eine Krankheit ausgewaehlt (impfschutzGegeben)
   * wurde.
   */
  private atLeastOneKrankheitSelectedValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const impfentscheide = control as FormGroup;
      const atLeastOneSelected = Object.values(impfentscheide.controls).some(
        krankheitGroup =>
          (krankheitGroup as UntersuchungImpfstatusSubFormType).controls['impfschutzGegeben'].value === true,
      );
      return atLeastOneSelected
        ? null
        : {[AppValidatorKeys.atLeastOneRequired]: true};
    };
  }

  /**
   * Krankheiten werden neu über die AnlassConfig definiert und sind eventuell beim Laden der Seite noch nicht
   * vorhanden, deshalb werden die SubForms über diese Funktion erstellt und die config überschrieben.
   */
  createKrankheitenSubForms(krankheiten: Krankheit[], data: UntersuchungImpfstatusListSaveJson | Nullish): void {
    this.config.krankheiten = krankheiten;

    Object.keys(this.group.controls.impfentscheide.controls)
      .filter(controlName => !krankheiten.some(krankheit => krankheit === controlName))
      .forEach(krankheit => this.group.controls.impfentscheide.removeControl(krankheit));

    krankheiten.forEach(krankheit => {
      this.group.controls.impfentscheide.addControl(krankheit, makeSubform(this.config, krankheit))
    });

    if (isNotNullish(data)) {
      this.resetForm(data);
    }
  }
}
