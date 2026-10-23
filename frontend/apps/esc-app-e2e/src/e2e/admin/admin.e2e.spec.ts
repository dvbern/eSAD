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

import {expect, test} from '../../fixtures/test.fixture';
import {AdminPageObject} from './page-objects/admin.page-object';
import {authStatePath} from '../../util/env.util';

test.describe('feat admin', () => {
  test.use({storageState: authStatePath('ESC_SUPPORT')});

  test('should show all links', async ({page}) => {
    const adminPage = new AdminPageObject(page);
    await adminPage.goto();

    const expectedLinks = [
      ' Schulen & Kindergärten ',
      ' Klassen ',
      ' Labels ',
      ' Impfstoffe ',
      ' Infomaterial ',
      ' Ärzte/Ärztinnen ',
      ' Benutzer & Rechte ',
      ' Briefvorlagen verwalten ',
      ' Duplikate verwalten ',
    ];

    await expect(adminPage.elements.navigationList()).toHaveText(expectedLinks);
  });
});
