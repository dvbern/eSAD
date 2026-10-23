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

import { expect, test } from '../../fixtures/test.fixture';
import { KinderListPageObject } from './page-objects/kinder-list.page-object';
import { KinderPageObject } from './page-objects/kinder.page-object';
import { createKind, openKindFromList } from '../../routines/kind.routines';
import {authStatePath} from '../../util/env.util';

/**
 * Finding a Kind and opening its dossier is how most work in the application starts, through either
 * the Kinder list or the global search in the header.
 */
test.describe('kind finden', () => {
  test.use({ storageState: authStatePath('ESC_SUPPORT') });

  test('kind in der liste filtern und dossier oeffnen', async ({ page, testData }) => {
    const kinderPage = new KinderPageObject(page);
    const kind = testData.kindForListFilter;
    await createKind(page.request, kind);

    await openKindFromList(page, kind);

    await expect(kinderPage.elements.kindName()).toHaveText(`${kind.nachname} ${kind.vorname}`);
  });

  test('kind ueber die globale suche oeffnen', async ({ page, testData }) => {
    const kinderList = new KinderListPageObject(page);
    const kinderPage = new KinderPageObject(page);
    const kind = testData.kindForSearch;
    await createKind(page.request, kind);

    await kinderList.goto();

    const search = kinderList.elements.globalKindSearch();
    await search.click();
    await search.fill(kind.nachname);
    await page.getByRole('option', { name: new RegExp(`${kind.nachname} ${kind.vorname}`) }).click();

    await kinderPage.isCurrentPage('detail');
    await expect(kinderPage.elements.kindName()).toHaveText(`${kind.nachname} ${kind.vorname}`);
  });
});
