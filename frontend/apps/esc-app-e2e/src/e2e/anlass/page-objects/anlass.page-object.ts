/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

import {FormDatePickerComponentObject} from '../../../component-objects/form-date-picker.component.object';
import {FormInputComponentObject} from '../../../component-objects/form-input.component-object';
import {FormSelectComponentObject} from '../../../component-objects/form-select.component-object';
import {DetailFormWithModePageObject} from '../../../page-objects';
import {patternBackendEntityId} from '../../../util/url-e2e.utils';

export class AnlassPageObject extends DetailFormWithModePageObject {

  selector = 'feat-anlass-detail-anlassdaten';

  paths = {
    create: '/anlass/create',
    edit: `/anlass/${patternBackendEntityId}/anlassbearbeitung`,
    view: '',
  };

  elements = {
    anlassTitleInput: () => new FormInputComponentObject('anlass-titel-input'),
    anlassArtSelect: () => new FormSelectComponentObject('anlass-art-select', 'svui-form-control'),
    anlassTypSelect: () => new FormSelectComponentObject('anlass-typ-select', 'svui-form-control'),
    anlassDatumDatePicker: () => new FormDatePickerComponentObject('anlass-datum-date-picker'),
    schuljahrSelect: () => new FormSelectComponentObject('anlass-schuljahr-select', 'svui-form-control'),
    schuleSelect: () => new FormSelectComponentObject('anlass-schule-select', 'svui-form-control'),
    klasseSelect: () => new FormSelectComponentObject('anlass-klasse-select', 'svui-form-control'),
    buttonAddKinder: () => this.page.getByTestId('button-add-kinder'),
    selectedKinderTitle: () => this.page.getByTestId('anlass-selected-kinder-title'),
    buttonRemoveKinder: () => this.page.getByTestId('button-remove-kinder'),
    kinderNameInput: () => new FormInputComponentObject('kinder-name-input'),
    buttonConfirmRemove: () => this.page.getByTestId('button-confirm'),
    buttonSaveAnlass: () => this.page.getByTestId('button-save'),
  }
}
