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
import { AnlassListPageObject } from './page-objects/anlass-list.page-object';
import { AnlassPlanungPageObject } from './page-objects/anlass-planung.page-objects';
import {fileupload} from "../../util/file-upload.util";

test.describe('anlass edit', () => {
  test.use({ storageState: './playwright/.auth/ESC_SUPPORT' });
  test.describe.configure({ mode: 'serial' });

  test.beforeEach(async ({ page, testData }) => {
    const anlassList = new AnlassListPageObject(page);
    await anlassList.goto();
    await anlassList.elements.linkAnlass().filter({ hasText: testData.anlass1.title}).first().click();
  });

  test('add impfbuch to kinder', async ({ page }) => {

    const anlassPlanungPage = new AnlassPlanungPageObject(page);
    await anlassPlanungPage.elements.buttonAddImpfausweis().first().click();

    await fileupload(page, 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf');

    await expect(anlassPlanungPage.elements.buttonAddImpfausweisSuccess()).toBeVisible();
  });

  test('add fragebogen to kinder', async ({ page }) => {
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await anlassPlanungPage.elements.buttonAddFragebogen().first().click();

    await fileupload(page, 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf');

    await expect(anlassPlanungPage.elements.buttonAddFragebogenSuccess()).toBeVisible();
  });

  test('add impfeinverstaendnis to kinder', async ({ page }) => {
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await anlassPlanungPage.elements.buttonAddImpfeinverstaendnis().first().click();

    await fileupload(page, 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf');

    await expect(anlassPlanungPage.elements.buttonAddImpfeinverstaendnisSuccess()).toBeVisible();
  });

  test.describe('change teilnahme status', () => {
    test('change teilnahme status to nachzuegler', async ({ page, testData }) => {
      const anlassPlanungPage = new AnlassPlanungPageObject(page);

      const row = anlassPlanungPage.elements.einladungenTable().row(page, 0);
      await anlassPlanungPage.elements.teilnahmeStatusSelect().setOption(page, 'Nachzügler', row);

      await expect (
        page.getByRole('row').filter({
          hasText: 'Nachzügler',
        })
      ).toBeVisible();

      await anlassPlanungPage.elements.buttonAufgabenList().click();

      await anlassPlanungPage.elements.buttonResetFilter().click({timeout: 3000});

      await page.waitForTimeout(3000);
      const expectedAufgabenTitel = new RegExp(`Nachzügler Musterkind Max 20\\d{2}/\\d{2} DIV ${testData.anlass1.title}`);

      await expect (
        page.getByRole('row').filter({
          hasText: expectedAufgabenTitel,
        })
      ).toBeVisible();
    });

    test('change teilnahme status to keine teilnahme', async ({ page, testData }) => {
      const anlassPlanungPage = new AnlassPlanungPageObject(page);
      const row = anlassPlanungPage.elements.einladungenTable().row(page, 0);
      await anlassPlanungPage.elements.teilnahmeStatusSelect().setOption(page, 'Keine Teilnahme', row);

      await anlassPlanungPage.elements.abmeldungGrundSelect().setOption(page, 'Regelmässige ärztliche Kontrollen');
      await anlassPlanungPage.elements.abmeldungArtSelect().setOption(page, 'Mail von Lehrperson');

      await anlassPlanungPage.elements.buttonSaveAbmeldung().click();
      await expect (
        page.getByRole('row').filter({
          hasText: 'Keine Teilnahme',
        })
      ).toBeVisible();

      await anlassPlanungPage.elements.linkKinderDossier().filter({ hasText: 'Musterkind' }).click();

      await expect (
        page.getByTestId('redirect-table-link').filter({
          hasText: new RegExp(`Abgemeldet von 20\\d{2}/\\d{2} DIV ${testData.anlass1.title}`),
        })

      ).toBeVisible();

    })
  });


  test('change anlass status to ready', async ({ page }) => {
    const anlassList = new AnlassListPageObject(page);
    await anlassList.elements.anlassStatusSelect().setOption(page, 'Bereit');

    await expect (
      page.locator('ng-select .ng-value-label', { hasText: 'Bereit' })
    ).toBeVisible();
  })
});
