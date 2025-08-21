import {AbstractControl, FormArray, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {ISOYearMonthString} from '@dvb/data-access-types';
import {AhvNummerUtil, isFilledIn, isNotNullish, isNullish, toDateStr} from '@dvb/util-shared';
import {addYears, subYears} from 'date-fns';

/**
 * Make sure that keys and values are the same.
 */
type ValidatorKeyMap<Keys extends object> = {
  [key in keyof Keys]: key;
};

// FIXME: used by the error control for translations
export const AppValidatorKeys = {
  date: 'date',
  dateRange: 'dateRange',
  dbMaxLength: 'dbMaxLength',
  textareaMaxLength: 'textareaMaxLength',
  nonNegativeNumber: 'nonNegativeNumber',
  numberOrNumericChars: 'numberOrNumericChars',
  backend: 'backend',
  unique: 'unique',
  email: 'email',
  phone: 'phone',
  nonFutureDates: 'nonFutureDates',
  nonPastDates: 'nonPastDates',
  zeitVonAfterZeitBis: 'zeitVonAfterZeitBis',
  ahvNummerPattern: 'ahvNummerPattern',
  ahvNummerInvalid: 'ahvNummerInvalid',
  plz: 'plz',
  ort: 'ort',
  equals: 'equals',
  atLeastOneRequired: 'atLeastOneRequired',
  publicUrl: 'publicUrl',
  esadId: 'esadId',
  minYear: 'minYear',
} as const satisfies ValidatorKeyMap<AppValidators>;

const MIN_KIND_AGE = 3;
const MAX_KIND_AGE = 130;

export class AppValidators {

  static requiredIfValidator(predicate: () => boolean): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (predicate()) {
        return Validators.required(control);
      }
      return null;
    };
  }

  /**
   * Only allow number characters, no negative/decimals.
   */
  static numberOrNumericChars(): ValidatorFn {
    return control => {
      const errors = Validators.pattern(/^\d+$/)(control);

      return errors
        ? {
          [AppValidatorKeys.numberOrNumericChars]: {
            actualValue: control.value,
          },
        }
        : null;
    };
  }

  /**
   * No Negativ Numbers
   */
  static nonNegativNumber(): ValidatorFn {
    return control => {
      const errors = Validators.min(0.0)(control);
      return errors
        ? {
          [AppValidatorKeys.nonNegativeNumber]: {
            actualValue: control.value,
          },
        }
        : null;
    };
  }

  /**
   * Date
   */
  static date(): ValidatorFn {
    return control => {
      const date = new Date(control.value);
      return !isFilledIn(control.value) || isNaN(date.valueOf())
        ? {
          [AppValidatorKeys.date]: {},
        }
        : null;
    };
  }

  /**
   * Date Range
   */
  static dateRange(afterDate: Date, beforeDate: Date): ValidatorFn {
    return control => {
      const dateErrors = AppValidators.date()(control);
      if (dateErrors) {
        return dateErrors;
      }

      const date = new Date(control.value);

      if (date.getTime() >= afterDate.getTime() && date.getTime() <= beforeDate.getTime()) {
        return null; // the date is valid
      }

      return {
        [AppValidatorKeys.dateRange]: {
          beforeDate: toDateStr(afterDate),
          afterDate: toDateStr(beforeDate),
        },
      };
    };
  }

  static aufgabeFaelligBisValidator(stichtag: Date): ValidatorFn {
    // very basic sanity check to protect against typos while still allowing to edit old entries for the foreseeable
    // future
    const minDate: Date = subYears(stichtag, 50);
    const maxDate: Date = addYears(stichtag, 50);
    return this.dateRange(minDate, maxDate);
  }

  static geburtsdatumKindValidator(currentDate: Date): ValidatorFn {
    // children must be at least 3 years old today, so they must be born before max date
    const minDate: Date = subYears(currentDate, MAX_KIND_AGE);
    const maxDate: Date = subYears(currentDate, MIN_KIND_AGE);
    return this.dateRange(minDate, maxDate);
  }

  static dbMaxLength(): ValidatorFn {
    const DB_DEFAULT_MAX_LENGTH = 255;

    return control => {
      const originalError = Validators.maxLength(DB_DEFAULT_MAX_LENGTH)(control);
      if (originalError) {
        return {[AppValidatorKeys.dbMaxLength]: originalError['maxlength']};
      }
      return null;
    };
  }

  static textareaMaxLength(): ValidatorFn {
    const DB_TEXTAREA_MAX_LENGTH = 2000;

    return control => {
      const originalError = Validators.maxLength(DB_TEXTAREA_MAX_LENGTH)(control);
      if (originalError) {
        return {[AppValidatorKeys.textareaMaxLength]: originalError['maxlength']};
      }
      return null;
    };
  }

  static emailValidator(): ValidatorFn {
    return (control) => {
      // Validators.email ist zu wenig streng (schaut nur aufs @, aber nicht auf .ch etc.)
      // Deshalb wollen wir eine Validierung mit Pattern, die aber "email" heisst (für eine schöne Fehlerbeschreibung)
      const EMAIL_PATTERN = '[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}';
      if (Validators.pattern(EMAIL_PATTERN)(control)) {
        return {[AppValidatorKeys.email]: true};
      }
      return null;
    };
  }

  // Wurde aus dem alten ePortal übernommen
  static phoneValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      // erlaubt Präfix + oder 0 oder 00; danach Ziffern oder Klammern und Striche
      const REGEX_TELEFON_LOOSE = '^(\\+|0|00)[1-9][0-9 \\-\\(\\)\\.\\/]{7,}$';
      if (Validators.pattern(REGEX_TELEFON_LOOSE)(control)) {
        return {[AppValidatorKeys.phone]: true};
      }
      return null;
    };
  }

  static plzValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {

      // Erlaubt nur 4 Ziffern
      const REGEX_PLZ = '^[0-9]{4}$';

      if (Validators.pattern(REGEX_PLZ)(control)) {
        return {[AppValidatorKeys.plz]: true};
      }
      return null;
    };
  }

  static ortValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      // ^[a-zA-ZäöüÄÖÜ] Muss mit einem Buchstaben anfangen
      // [a-zA-Z0-9 äöüÄÖÜ.-]* Erlaubt Buchstaben, Leerzeichen, Punkt, Bindestrich
      const REGEX_ORT = /^[\p{L}-]*$/u;

      if (Validators.pattern(REGEX_ORT)(control)) {
        return {[AppValidatorKeys.ort]: true};
      }
      return null;
    };
  }

  static ahvNummerValdiator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const AHV_NUMMER_REGEX = '^756\\.\\d{4}\\.\\d{4}\\.\\d{2}$';
      if (isNullish(control.value) || control.value === '') {
        return null;
      } else if (Validators.pattern(AHV_NUMMER_REGEX)(control)) {
        return {[AppValidatorKeys.ahvNummerPattern]: true};
      } else {
        const parsedAhvNummer = AhvNummerUtil.parseAhvNummer(control.value);
        if (!AhvNummerUtil.validateAhvNummer(parsedAhvNummer)) {
          return {[AppValidatorKeys.ahvNummerInvalid]: true};
        } else {
          return null;
        }
      }
    };
  }

  static nonPastDates(todayDate: Date): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (isNullish(control.value)) {
        return null;
      }
      const dateErrors = AppValidators.date()(control);
      if (dateErrors) {
        return dateErrors;
      }

      const inputDate = new Date(control.value);

      if (inputDate.setHours(0, 0, 0, 0) < todayDate.setHours(0, 0, 0, 0)) {
        return {[AppValidatorKeys.nonPastDates]: true};
      }

      return null;
    };
  }

  static nonFutureDates(predicateOrUndefined: (() => boolean) | undefined, todayDate: Date): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (predicateOrUndefined && predicateOrUndefined()) {
        if (isNullish(control.value)) {
          return null;
        }
        const dateErrors = AppValidators.date()(control);
        if (dateErrors) {
          return dateErrors;
        }

        const inputDate = new Date(control.value);

        if (inputDate.setHours(0, 0, 0, 0) > todayDate.setHours(0, 0, 0, 0)) {
          return {[AppValidatorKeys.nonFutureDates]: true};
        }
      }

      return null;
    };
  }

  static zeitBisIsAfterZeitVon(nameVon: string, nameBis: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const zeitVon: string = control.get(nameVon)?.value;
      const zeitBis = control.get(nameBis)?.value;

      if (isNullish(zeitVon) || isNullish(zeitBis)) {
        return null;
      }

      const [hoursVon, minutesVon]: number[] = zeitVon.split(':').map(Number);
      const [hoursBis, minutesBis]: number[] = zeitBis.split(':').map(Number);

      const datumVon = new Date();
      datumVon.setUTCHours(hoursVon, minutesVon, 0, 0);

      const datumBis = new Date();
      datumBis.setUTCHours(hoursBis, minutesBis, 0, 0);

      if (datumVon >= datumBis) {
        return {[AppValidatorKeys.zeitVonAfterZeitBis]: true};
      }

      return null;
    };
  }

  static requireEquals(predicate: () => string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (predicate() !== control.value) {
        return {[AppValidatorKeys.equals]: true};
      }
      return null;
    };
  }

  static atLeastOneCheckedValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const formArray = control as FormArray;

      const atLeastOneChecked = formArray.controls.some(ctrl => ctrl.get('isChecked')?.value === true);

      formArray.controls.forEach(ctrl => {
        const isCheckedControl = ctrl.get('isChecked');
        if (isCheckedControl) {
          if (!atLeastOneChecked) {
            isCheckedControl.setErrors({[AppValidatorKeys.atLeastOneRequired]: true});
          } else {
            isCheckedControl.setErrors(null);
          }
        }
      });

      return atLeastOneChecked ? null : {[AppValidatorKeys.atLeastOneRequired]: true};
    };
  }

  static publicUrlValidator(publicUrl: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (control.value && !control.value?.startsWith(publicUrl)) {
        return {[AppValidatorKeys.publicUrl]: true};
      }
      return null;
    };
  }

  static esadIdValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {

      const regex = /^\d{9}$/;
      const esadId = control.value?.replace(/\s/g, '');
      if (!regex.test(esadId)) {
        return {[AppValidatorKeys.esadId]: true};
      }
      return null;
    };
  }

  static minYear(minYear: number): ValidatorFn {
    return control => {
      if (isNotNullish(control.value)) {
        const value: ISOYearMonthString = control.value;
        const year = +value.split('-')[0];
        if (year < minYear) {
          return {[AppValidatorKeys.minYear]: {year: minYear} };
        }
      }
      return null;
    };
  }
}
