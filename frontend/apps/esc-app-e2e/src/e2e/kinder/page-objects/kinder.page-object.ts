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


import {patternBackendEntityId} from '../../../util/url-e2e.utils';
import {WithSubpaths} from "../../../page-objects/with-subpahts";

export class KinderPageObject extends WithSubpaths<'detail' | 'view'> {

  // The Kind dossier, not the Anlass detail page this used to point at.
  selector = 'feat-kinder-detail-dossier';

  paths = {
    detail: `/kinder/${patternBackendEntityId}`,
    view: '',
  };

  elements = {
    buttonAddAnlass: () => this.page.getByTestId('button-add-anlass'),
    buttonAddNotiz: () => this.page.getByTestId('button-add-notiz'),
    // Scoped to the Personalien card: Aufgaben rows in the dossier are headings starting with the
    // Kind's name too.
    kindName: () => this.page.locator('feat-kind-dossier-main').getByRole('heading').first(),
    anlaesseList: () => this.page.locator('feat-kind-dossier-anlaesse-list'),
    notizenList: () => this.page.locator('feat-kind-notizen-list'),
  }
}
