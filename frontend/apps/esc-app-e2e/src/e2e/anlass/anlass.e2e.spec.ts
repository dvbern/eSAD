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

import {expect, test} from '@playwright/test';
import {addMonths} from 'date-fns';
import {AnlassListPageObject} from './page-objects/anlass-list.page-object';
import {AnlassPageObject} from './page-objects/anlass.page-object';

test.describe('anlass', () => {
  test.use({storageState: './playwright/.auth/ESC_SUPPORT'});

  test('create anlass', async ({page}) => {
    const anlassList = new AnlassListPageObject(page);
    // local tests - when a fix id is set in liquibase - maybe not very useful
    const anlassFormPage = new AnlassPageObject(page);
    const anlassDate = addMonths(new Date(), 2);

    await anlassList.goto();
    await anlassList.elements.buttonHinzufuegen().click();

    await anlassFormPage.isCurrentPage('create');

    await anlassFormPage.elements.titleInput().getInput(page).fill('Test Anlass 1');
    await anlassFormPage.elements.anlassArtSelect().setOption(page, 'Gruppe');
    await anlassFormPage.elements.anlassTypSelect().setOption(page, 'Andere');
    await anlassFormPage.elements.anlassDatumDatePicker().setDate(page, anlassDate);

    const [request] = await Promise.all([
        page.waitForRequest(request =>
          request.url().includes('/api/anlaesse') && request.method() === 'POST'),
        anlassFormPage.baseForm.submitButton().click(),
      ],
    );

    const postData = request.postDataJSON();
    expect(postData).toEqual({
      anlassArt: 'GRUPPEN_ANLASS',
      anlassStatus: 'IN_PLANUNG',
      anlassTyp: 'VERTRAUENSARZT',
      arzt: null,
      datum: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/), //format(anlassDate, 'yyyy-MM-dd'),
      mpa: null,
      schuljahr: getSchoolYear(anlassDate),
      titel: 'Test Anlass 1',
      weiterePerson: null,
    });

    await anlassFormPage.isCurrentPage('edit');
  });
});

function getSchoolYear(date: Date) {
  if (date.getMonth() < 7) {
    return date.getFullYear() - 1;
  }
  return date.getFullYear();
}
