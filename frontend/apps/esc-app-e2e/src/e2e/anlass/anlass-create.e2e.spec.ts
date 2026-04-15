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
import { addMonths } from 'date-fns';
import { AnlassListPageObject } from './page-objects/anlass-list.page-object';
import { AnlassPageObject } from './page-objects/anlass.page-object';
import { AnlassPlanungPageObject } from './page-objects/anlass-planung.page-objects';
import { KinderListPageObject } from '../kinder/page-objects/kinder-list.page-object';
import { KinderPageObject } from '../kinder/page-objects/kinder.page-object';

test.describe('anlass create', () => {
  test.use({ storageState: './playwright/.auth/ESC_SUPPORT' });
  test.describe.configure({ mode: 'serial' });

  test('create anlass', async ({ page, testData }) => {
    const anlassList = new AnlassListPageObject(page);
    // local tests - when a fix id is set in liquibase - maybe not very useful
    const anlassFormPage = new AnlassPageObject(page);
    const anlassDate = addMonths(new Date(), 2);

    await anlassList.goto();
    await anlassList.elements.buttonHinzufuegen().click();

    await anlassFormPage.isCurrentPage('create');

    await anlassFormPage.elements.anlassTitleInput().getInput(page).fill(testData.anlass1.title);
    await anlassFormPage.elements.anlassArtSelect().setOption(page, 'Gruppe');
    await anlassFormPage.elements.anlassTypSelect().setOption(page, 'Andere');
    await anlassFormPage.elements.anlassDatumDatePicker().setDate(page, anlassDate);

    const [request] = await Promise.all([
      page.waitForRequest(request => request.url().includes('/api/anlaesse') && request.method() === 'POST'),
      anlassFormPage.baseForm.submitButton().click(),
    ]);

    const postData = request.postDataJSON();
    expect(postData).toEqual({
      anlassConfigOption : {
        id: expect.any(String),
        anlassTyp : 'VERTRAUENSARZT',
        datumVon: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        datumBis: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        bezeichnung: 'Default',
    },
      titel: testData.anlass1.title,
      anlassStatus: 'IN_PLANUNG',
      anlassTyp : 'VERTRAUENSARZT',
      anlassArt: 'GRUPPEN_ANLASS',
      arzt: null,
      datum: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/), //format(anlassDate, 'yyyy-MM-dd'),
      schuljahr: getSchoolYear(anlassDate),
      mpa: null,
      weiterePerson: null,
      version: expect.any(Number),
    })

    await anlassFormPage.isCurrentPage('edit');
  });

  test('create anlass 4 primar', async ({ page, testData }) => {
    const anlassList = new AnlassListPageObject(page);
    const anlassFormPage = new AnlassPageObject(page);
    const anlassDate = new Date();

    await anlassList.goto();
    await anlassList.elements.buttonHinzufuegen().click();

    await anlassFormPage.isCurrentPage('create');

    await anlassFormPage.elements.anlassTitleInput().getInput(page).fill(testData.anlass2.title);
    await anlassFormPage.elements.anlassArtSelect().setOption(page, 'Gruppe');
    await anlassFormPage.elements.anlassTypSelect().setOption(page, '4. Primar');
    await anlassFormPage.elements.anlassDatumDatePicker().setDate(page, anlassDate);
    await anlassFormPage.elements.schuljahrSelect().setOption(page, '2025/26');

    const [request] = await Promise.all([
      page.waitForRequest(request => request.url().includes('/api/anlaesse') && request.method() === 'POST'),
      anlassFormPage.baseForm.submitButton().click(),
    ]);

    const postData = request.postDataJSON();

    expect(postData).toEqual({
      anlassConfigOption : {
        anlassTyp : 'KLASSE_4',
        id: expect.any(String),
        datumVon: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        datumBis: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        bezeichnung: 'Default',
      },
      anlassArt: 'GRUPPEN_ANLASS',
      anlassStatus: 'IN_PLANUNG',
      anlassTyp: 'KLASSE_4',
      arzt: null,
      datum: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
      mpa: null,
      schuljahr: getPreviousYear(anlassDate),
      titel: testData.anlass2.title,
      version: expect.any(Number),
      weiterePerson: null,
    });
    await anlassFormPage.isCurrentPage('edit');
  });

  test('add kinder to anlass', async ({ page, testData }) => {
    const anlassList = new AnlassListPageObject(page);
    const anlassFormPage = new AnlassPageObject(page);
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await anlassList.goto();
    await page.getByRole('link', { name: testData.anlass1.title }).first().click();

    await anlassPlanungPage.elements.buttonBearbeiten().click();

    await anlassFormPage.isCurrentPage('edit');

    await anlassFormPage.elements.schuleSelect().setOption(page, 'Hochstrassschulhaus');
    await anlassFormPage.elements.klasseSelect().setOption(page, 'Klasse 4a');

    await anlassFormPage.elements.buttonAddKinder().click();

    const rows = page.getByTestId('anlass-selected-kinder-table').locator('tbody tr');
    await expect(rows).toHaveCount(2, { timeout: 5000 });
  });

  test('remove kinder in anlass', async ({ page, testData }) => {

    const anlassList = new AnlassListPageObject(page);
    const anlassFormPage = new AnlassPageObject(page);
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await anlassList.goto();
    await page.getByRole('link', { name: testData.anlass1.title }).first().click();

    await anlassPlanungPage.elements.buttonBearbeiten().click();

    await anlassFormPage.isCurrentPage('edit');

    await anlassFormPage.elements.selectedKinderTitle().click();
    await anlassFormPage.elements.buttonRemoveKinder().first().click();

    await anlassFormPage.elements.kinderNameInput().getInput(page).fill('Müller Elvira');
    await anlassFormPage.elements.buttonConfirmRemove().click();

    const rows = page.getByTestId('anlass-selected-kinder-table').locator('tbody tr');
    await expect(rows).toHaveCount(1, { timeout: 5000 });
  });

  test('add anlass in kinder', async ({ page, testData }) => {
    const kinderList = new KinderListPageObject(page);
    const kinderFormPage = new KinderPageObject(page);
    const anlassFormPage = new AnlassPageObject(page);
    const anlassDate = new Date();

    await kinderList.goto();
    await kinderList.elements.linkKinder().first().click();

    await kinderFormPage.elements.buttonAddAnlass().click();

    await anlassFormPage.isCurrentPage('create');

    await anlassFormPage.elements.anlassTitleInput().getInput(page).fill(testData.anlass3.title);
    await anlassFormPage.elements.anlassArtSelect().setOption(page, 'Einzel');
    await anlassFormPage.elements.anlassTypSelect().setOption(page, 'Impftermin');
    await anlassFormPage.elements.anlassDatumDatePicker().setDate(page, anlassDate);

    const [request] = await Promise.all([
      page.waitForRequest(req => req.url().includes('/api/anlaesse') && req.method() === 'POST'),
      anlassFormPage.baseForm.submitButton().click(),
      await anlassFormPage.elements.buttonSaveAnlass().click()
    ]);

    const postData = request.postDataJSON();

    expect(postData).toEqual({
      anlassConfigOption : {
        anlassTyp : 'IMPFTERMIN',
        id: expect.any(String),
        datumVon: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        datumBis: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        bezeichnung: 'Default',
      },
      anlassArt: 'EINZEL_ANLASS',
      anlassStatus: 'IN_PLANUNG',
      anlassTyp: 'IMPFTERMIN',
      arzt: null,
      datum: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
      mpa: null,
      schuljahr: getSchoolYear(anlassDate),
      titel:  testData.anlass3.title,
      version: expect.any(Number),
      weiterePerson: null,
    });

    await anlassFormPage.isCurrentPage('edit');
    await anlassFormPage.elements.buttonSaveAnlass().click();

    await expect(page.locator('feat-anlass-planung-anlassdaten-daten')).toBeVisible();
  });
});

function getSchoolYear(date: Date): number {
  if (date.getMonth() < 7) {
    return date.getFullYear() - 1;
  }
  return date.getFullYear();
}

function getPreviousYear(date: Date): number {
  return date.getFullYear() - 1;
}
