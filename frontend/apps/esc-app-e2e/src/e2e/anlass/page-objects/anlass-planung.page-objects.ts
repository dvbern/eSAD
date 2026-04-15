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
import { patternBackendEntityId } from '../../../util/url-e2e.utils';
import { FormSelectComponentObject } from '../../../component-objects/form-select.component-object';
import {TableComponentObject} from "../../../component-objects/table.component-object";


export class AnlassPlanungPageObject extends BasePageObject {
  path = `/anlass/${patternBackendEntityId}/anlassplanung`;
  selector = 'feat-anlass-detail-page';

  elements = {
    teilnahmeStatusSelect: () => new FormSelectComponentObject('teilnahme-status-select', 'svui-table-control'),
    abmeldungGrundSelect: () => new FormSelectComponentObject('abmeldung-grund-select', 'svui-form-control'),
    abmeldungArtSelect: () => new FormSelectComponentObject('abmeldung-art-select', 'svui-form-control'),
    linkKinderDossier: () => this.page.getByTestId('redirect-kinder-dossier'),


    einladungenTable: () => new TableComponentObject('einladungen-table'),

    buttonBearbeiten: () => this.page.getByTestId('button-anlass-bearbeiten'),
    buttonAufgabenList: () => this.page.getByTestId('button-aufgaben-list'),
    buttonResetFilter: () => this.page.getByTestId('button-reset-filter'),
    buttonSaveAbmeldung: () => this.page.getByTestId('button-confirm'),
    buttonAddImpfausweis: () => this.page.getByTestId('button-add-impfausweis'),
    buttonAddImpfausweisSuccess: () => this.page.getByTestId('button-add-impfausweis-success'),
    buttonAddFragebogen: () => this.page.getByTestId('button-add-fragebogen'),
    buttonAddFragebogenSuccess: () => this.page.getByTestId('button-add-fragebogen-success'),
  };
}
