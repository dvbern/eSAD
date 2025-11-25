import {inject, Pipe, PipeTransform} from '@angular/core';
import {ValidationErrors, Validators} from '@angular/forms';
import {AppValidatorKeys} from '@dvb/util-angular';
import {TranslationKey, TranslationMap} from '@dvb/util-i18n';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

// Sort Alphabetically
const ErrorMessageTranslations: TranslationMap<typeof AppValidatorKeys> | Record<Lowercase<keyof typeof Validators>, TranslationKey> = {
  ahvNummerInvalid: 'ui.validation-error.ahvNummerInvalid',
  ahvNummerPattern: 'ui.validation-error.ahvNummerPattern',
  atLeastOneRequired: 'ui.validation-error.atLeastOneRequired',
  backend: 'ui.validation-error.backend',
  date: 'ui.validation-error.date',
  dateRange: 'ui.validation-error.dateRange',
  dbMaxLength: 'ui.validation-error.dbMaxLength',
  email: 'ui.validation-error.email',
  equals: 'ui.validation-error.equals',
  esadId: 'ui.validation-error.esadId',
  max: 'ui.validation-error.max',
  maxlength: 'ui.validation-error.maxlength',
  min: 'ui.validation-error.min',
  minlength: 'ui.validation-error.minlength',
  minYear: 'ui.validation-error.minYear',
  nonFutureDates: 'ui.validation-error.nonFutureDates',
  nonNegativeNumber: 'ui.validation-error.nonNegativeNumber',
  nonPastDates: 'ui.validation-error.nonPastDates',
  numberOrNumericChars: 'ui.validation-error.numberOrNumericChars',
  oneDecimalPlace: "ui.validation-error.oneDecimalPlace",
  ort: 'ui.validation-error.ort',
  pattern: 'ui.validation-error.pattern',
  phone: 'ui.validation-error.phone',
  plz: 'ui.validation-error.plz',
  publicUrl: 'ui.validation-error.publicUrl',
  required: 'ui.validation-error.required',
  requiredtrue: 'ui.validation-error.requiredtrue',
  textareaMaxLength: 'ui.validation-error.textareaMaxLength',
  unique: 'ui.validation-error.unique',
  zeitVonAfterZeitBis: 'ui.validation-error.zeitVonAfterZeitBis',
  zeroDecimalPlaces: "ui.validation-error.zeroDecimalPlaces",
  changeSchuljahr: "ui.validation-error.changeSchuljahr",
};

function translationIsDefined(messageKey: string): messageKey is keyof typeof ErrorMessageTranslations {
  return Object.keys(ErrorMessageTranslations).includes(messageKey);
}

function findFirstError(errors: ValidationErrors | Nullish): string | undefined {
  if (errors) {
    return Object.keys(errors)[0];
  }
  return undefined;
}

@Pipe({
  name: 'controlMessagePipe',
  standalone: true,
})
export class SharedUiControlMessagePipe implements PipeTransform {
  readonly #transloco = inject(TranslocoService);

  transform(error: ValidationErrors | Nullish): string {
    if (isNullish(error)) {
      return '';
    }
    const firstError = findFirstError(error);
    if (firstError && translationIsDefined(firstError)) {
      // das ist ein bisschen fragil/nicht intuitiv mit den Translation-Args (sieht z.B. Validators.minLength)
      // Evtl. doch ein grosses switch ueber alle moeglichen Fehler?
      const messageKey = ErrorMessageTranslations[firstError];
      const validatorOutcome = error?.[firstError];
      return this.#transloco.translate(messageKey.toString(), validatorOutcome);
    }

    console.error('no error translation found for: ', firstError, 'error object: ', error);
    return `⚠️ No Translation for Validation Error Key '${firstError}' in '${JSON.stringify(error)}' ⚠️`;
  }
}
