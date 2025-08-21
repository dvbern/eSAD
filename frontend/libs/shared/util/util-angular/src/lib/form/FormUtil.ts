/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import { FormControl, FormGroup } from '@angular/forms';
import { LogFactory, Nullish, requireValue } from '@dvb/util-shared';

const LOG = LogFactory.createLog('FormUtil');

export class FormUtil {
  public static readonly INVALID_INPUT_FIELDS_CSS_SELECTOR =
    '.ng-invalid.focusable-field';

  public static selectFirstInvalid(): void {
    // focusable-field innerhalb eines invaliden Feldes. Aber nicht einfach das erste Element im invaliden Form.

    FormUtil.focus(FormUtil.INVALID_INPUT_FIELDS_CSS_SELECTOR);
  }

  public static autofocusField(): void {
    FormUtil.focus('.autofocus .focusable-field');
  }

  public static focus(cssSelector: string): void {
    const invalidFields = document.querySelectorAll(cssSelector);
    const tmp = invalidFields[0];
    if (tmp && tmp instanceof HTMLElement) {
      tmp.focus();
    }
  }

  /**
   * Touches all fields of the formGroup in order to show the validation of each field, especially
   * untouched ones.
   * If the form is invalid, jump to the first invalid field.
   * Otherwise, continue with the specified method.
   * ifNotValidBlock is a callback function
   *
   * FormGroup has one of 4 possible status: valid | invalid | disabled | pending
   */
  public static doIfValid(
    formGroup: FormGroup,
    submitBlock: () => unknown,
    htmlForm: HTMLFormElement,
    reset: boolean,
    ifNotValidBlock: (() => unknown) | undefined
  ): void {
    formGroup.markAllAsTouched();
    if (formGroup.valid) {
      submitBlock();
      if (reset) {
        // otherwise the form is still invalid because emptied
        // https://stackoverflow.com/questions/48216330/angular-5-formgroup-reset-doesnt-reset-validators
        this.resetForm(formGroup, htmlForm);
      }
    } else {
      LOG.info('Formular war nicht valid', formGroup);
      FormUtil.selectFirstInvalid();
      if (ifNotValidBlock) {
        ifNotValidBlock();
      }
    }
  }

  public static resetForm(
    formGroup: FormGroup,
    htmlForm: HTMLFormElement,
    value?: Record<string, unknown>
  ): void {
    // otherwise the form is still invalid because emptied
    // https://stackoverflow.com/questions/48216330/angular-5-formgroup-reset-doesnt-reset-validators

    htmlForm.reset();
    formGroup.reset(value);
  }

  public static resetFormAndEnable(
    formGroup: FormGroup,
    htmlForm: HTMLFormElement,
    value?: Record<string, unknown>
  ): void {
    formGroup.enable();
    this.resetForm(formGroup, htmlForm, value);
  }

  public static isFormDirty(formGroup: FormGroup): boolean {
    return formGroup.dirty;
  }

  public static removeErrorFromControl(
    control: FormControl<string | Nullish>,
    errorKeyToRemove: string
  ): void {
    if (control.hasError(errorKeyToRemove)) {
      // this removes the key of the err from the errors obj
      delete requireValue(control.errors)[errorKeyToRemove];
      control.updateValueAndValidity();
    }
  }

  public static clearAndDisable(control: FormControl): void {
    control.reset();
    control.disable();
  }
}
