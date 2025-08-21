import {inject, Pipe, PipeTransform} from '@angular/core';
import {ValidationErrors, Validators} from '@angular/forms';
import {AppValidatorKeys} from '@dvb/util-angular';
import {TranslationKey, TranslationMap} from '@dvb/util-i18n';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

const ErrorMessageTranslations: TranslationMap<typeof AppValidatorKeys> | Record<Lowercase<keyof typeof Validators>, TranslationKey> = {
  max: 'ui.validation-error.max',
  min: 'ui.validation-error.min',
  pattern: 'ui.validation-error.pattern',
  required: 'ui.validation-error.required',
  requiredtrue: 'ui.validation-error.requiredtrue',
  date: 'ui.validation-error.date',
  dateRange: 'ui.validation-error.dateRange',
  dbMaxLength: 'ui.validation-error.dbMaxLength',
  textareaMaxLength: 'ui.validation-error.textareaMaxLength',
  email: 'ui.validation-error.email',
  phone: 'ui.validation-error.phone',
  ahvNummerPattern: 'ui.validation-error.ahvNummerPattern',
  ahvNummerInvalid: 'ui.validation-error.ahvNummerInvalid',
  plz: 'ui.validation-error.plz',
  ort: 'ui.validation-error.ort',
  maxlength: 'ui.validation-error.maxlength',
  minlength: 'ui.validation-error.minlength',
  nonNegativeNumber: 'ui.validation-error.nonNegativeNumber',
  numberOrNumericChars: 'ui.validation-error.numberOrNumericChars',
  backend: 'ui.validation-error.backend',
  unique: 'ui.validation-error.unique',
  nonPastDates: 'ui.validation-error.nonPastDates',
  nonFutureDates: 'ui.validation-error.nonFutureDates',
  zeitVonAfterZeitBis: 'ui.validation-error.zeitVonAfterZeitBis',
  equals: 'ui.validation-error.equals',
  publicUrl: 'ui.validation-error.publicUrl',
  esadId: 'ui.validation-error.esadId',
  minYear: 'ui.validation-error.minYear',
  atLeastOneRequired: 'ui.validation-error.atLeastOneRequired',
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
