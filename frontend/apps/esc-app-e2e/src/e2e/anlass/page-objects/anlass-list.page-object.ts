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

import {BasePageObject} from '../../../page-objects';
import { FormSelectComponentObject } from '../../../component-objects/form-select.component-object';

export class AnlassListPageObject extends BasePageObject {
  path = '/anlass';
  selector = 'feat-anlass-list';

  elements = {
    buttonHinzufuegen: () => this.page.getByTestId('button-create-anlass'),
    linkAnlass: () => this.page.getByTestId('redirect-anlass-planung'),
    invitedKinderCheckbox: () => this.page.getByTestId('invited-kinder-checkbox'),
    buttonKinderAuswaehlen: () => this.page.getByTestId('button-kinder-auswaehlen'),
    anlassStatusSelect: () => new FormSelectComponentObject('anlass-status-select', 'ng-select')
  }
}
