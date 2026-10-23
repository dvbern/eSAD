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
import {expectAufgabeInList} from "../../routines/aufgaben.routines";
import {Locator, Page} from "@playwright/test";
import {authStatePath} from '../../util/env.util';

test.describe('anlass edit', () => {
  test.use({ storageState: authStatePath('ESC_SUPPORT') });
  test.describe.configure({ mode: 'serial' });

  test.beforeEach(async ({ page, testData }) => {
    const anlassList = new AnlassListPageObject(page);
    await anlassList.goto();
    await anlassList.elements.linkAnlass().filter({ hasText: testData.anlassEdit.title}).first().click();
  });

  /**
   * Uploading a document leaves the Einladungen list for the Notiz detail page, while the green
   * "already uploaded" marker lives on the list - so the test has to come back before asserting.
   * It re-opens the URL rather than going back in history: the Einladungen list is rendered from
   * the store's cached state, so a history back still shows the pre-upload row.
   */
  async function uploadDocumentForFirstKind(page: Page, uploadButton: Locator) {
    const anlassPlanungPage = new AnlassPlanungPageObject(page);
    const anlassPlanungUrl = page.url();

    await uploadButton.click();
    await fileupload(page, 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf');

    await page.goto(anlassPlanungUrl);
    await anlassPlanungPage.isCurrentPage();
  }

  test('add impfbuch to kinder', async ({ page }) => {
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await uploadDocumentForFirstKind(page, anlassPlanungPage.elements.buttonAddImpfausweis().first());

    await expect(anlassPlanungPage.elements.buttonAddImpfausweisSuccess().first()).toBeVisible();
  });

  test('add fragebogen to kinder', async ({ page }) => {
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await uploadDocumentForFirstKind(page, anlassPlanungPage.elements.buttonAddFragebogen().first());

    await expect(anlassPlanungPage.elements.buttonAddFragebogenSuccess().first()).toBeVisible();
  });

  test('add impfeinverstaendnis to kinder', async ({ page }) => {
    const anlassPlanungPage = new AnlassPlanungPageObject(page);

    await uploadDocumentForFirstKind(page, anlassPlanungPage.elements.buttonAddImpfeinverstaendnis().first());

    await expect(anlassPlanungPage.elements.buttonAddImpfeinverstaendnisSuccess().first()).toBeVisible();
  });

  test.describe('change teilnahme status', () => {
    test('change teilnahme status to nachzuegler', async ({ page, testData }) => {
      const anlassPlanungPage = new AnlassPlanungPageObject(page);

      const row = anlassPlanungPage.elements.einladungenTable().row(page, 0);
      const kind = await readKindFromRow(row);
      await anlassPlanungPage.elements.teilnahmeStatusSelect().setOption(page, 'Nachzügler', row);

      await expect (
        page.getByRole('row').filter({
          hasText: 'Nachzügler',
        })
      ).toBeVisible();

      await anlassPlanungPage.elements.buttonAufgabenList().click();
      await anlassPlanungPage.elements.buttonResetFilter().click();

      const expectedAufgabenTitel =
        new RegExp(`Nachzügler ${kind.name} ${kind.vorname} 20\\d{2}/\\d{2} DIV ${testData.anlassEdit.title}`);

      await expectAufgabeInList(page, testData.anlassEdit.title, expectedAufgabenTitel);
    });

    test('change teilnahme status to keine teilnahme', async ({ page, testData }) => {
      const anlassPlanungPage = new AnlassPlanungPageObject(page);
      const row = anlassPlanungPage.elements.einladungenTable().row(page, 0);
      const kind = await readKindFromRow(row);
      await anlassPlanungPage.elements.teilnahmeStatusSelect().setOption(page, 'Keine Teilnahme', row);

      await anlassPlanungPage.elements.abmeldungGrundSelect().setOption(page, 'Regelmässige ärztliche Kontrollen');
      await anlassPlanungPage.elements.abmeldungArtSelect().setOption(page, 'Mail von Lehrperson');

      await anlassPlanungPage.elements.buttonSaveAbmeldung().click();
      await expect (
        page.getByRole('row').filter({
          hasText: 'Keine Teilnahme',
        })
      ).toBeVisible();

      await anlassPlanungPage.elements.linkKinderDossier().filter({ hasText: kind.name }).click();

      await expect (
        page.getByTestId('redirect-table-link').filter({
          hasText: new RegExp(`Abgemeldet von 20\\d{2}/\\d{2} DIV ${testData.anlassEdit.title}`),
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

/**
 * Name and Vorname of the Kind in an Einladungen row. Which Kind sits in a given row depends on the
 * Anlass' data, so the tests read it instead of hard coding one.
 */
async function readKindFromRow(row: Locator): Promise<{ name: string; vorname: string }> {
  const nameColumn = 1;
  const vornameColumn = 2;
  const cells = row.locator('td');

  return {
    name: (await cells.nth(nameColumn).innerText()).trim(),
    vorname: (await cells.nth(vornameColumn).innerText()).trim(),
  };
}
