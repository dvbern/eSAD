import { expect } from '@playwright/test';
import path from 'path';
import { test } from '../../fixtures/test.fixture';
import { createKind, openKindFromList } from '../../routines/kind.routines';
import { KinderPageObject } from '../kinder/page-objects/kinder.page-object';
import { NotizenDetailPageObject } from './page-objects/notizen-detail.page-object';
import { NotizenPageObject } from './page-objects/notizen.page-object';
import {fileupload} from "../../util/file-upload.util";
import {authStatePath} from '../../util/env.util';

test.describe('Notizen', () => {
  test.use({ storageState: authStatePath('ESC_SUPPORT') });

  test('notiz erstellen', async ({ page, testData }) => {
    const kinderPage = new KinderPageObject(page);
    const notizFormPage = new NotizenPageObject(page)

    await createKind(page.request, testData.kindForNotiz);
    await openKindFromList(page, testData.kindForNotiz);
    const kindUrl = page.url();

    await kinderPage.elements.buttonAddNotiz().click();

    await notizFormPage.isCurrentPage('create');
    await notizFormPage.elements.notizTitleInput().getInput(page).fill(testData.notizTitle);
    await notizFormPage.elements.notizLabelSelect().setOption(page, 'Dispensationsgesuch');

    await fileupload(page, 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf')

    // Saving leaves the browser on the Notiz detail page, where the title is an input. The link
    // under test lives in the Notizen list of the Kind, so go back to the dossier to assert it.
    await page.goto(kindUrl);

    await expect(page.getByRole('link').filter({ hasText: testData.notizTitle })).toBeVisible();
  });

  test('notiz ohne titel zeigt validierungsfehler', async ({ page, testData }) => {
    const kinderPage = new KinderPageObject(page);
    const notizFormPage = new NotizenPageObject(page);

    await createKind(page.request, testData.kindForNotizValidation);
    await openKindFromList(page, testData.kindForNotizValidation);

    await kinderPage.elements.buttonAddNotiz().click();

    await notizFormPage.isCurrentPage('create');

    const titelControl = notizFormPage.elements.notizTitleInput().getFormField(page);

    // errors render only once the control is touched, not before the first submit attempt
    await expect(titelControl.locator('svui-form-control-error')).toHaveCount(0);

    await page.getByTestId('button-confirm').click();

    // Submitting marks the form touched. The error message is a plain control.touched/errors
    // template read, so it only repaints when the validity trigger's markForCheck schedules
    // change detection - the path zone.js used to cover for free.
    await expect(titelControl.locator('svui-form-control-error'))
      .toContainText('Feld erforderlich');

    // Typing clears it again, driven by the form's statusChanges alone.
    await notizFormPage.elements.notizTitleInput().getInput(page).fill(testData.notizTitle);

    await expect(titelControl.locator('svui-form-control-error')).toHaveCount(0);
  });

  test('hochgeladene datei vor dem speichern entfernen', async ({ page, testData }) => {
    const kinderPage = new KinderPageObject(page);
    const notizFormPage = new NotizenPageObject(page);
    const notizenDetailPage = new NotizenDetailPageObject(page);

    await createKind(page.request, testData.kindForNotizFileRemoval);
    await openKindFromList(page, testData.kindForNotizFileRemoval);

    await kinderPage.elements.buttonAddNotiz().click();

    await notizFormPage.isCurrentPage('create');
    await notizFormPage.elements.notizTitleInput().getInput(page).fill(testData.notizTitle);
    await notizFormPage.elements.notizLabelSelect().setOption(page, 'Dispensationsgesuch');

    const [fileChooser] = await Promise.all([
      page.waitForEvent('filechooser'),
      notizenDetailPage.elements.buttonAddFile().click(),
    ]);
    await fileChooser.setFiles(
      path.join(process.cwd(), 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf'),
    );

    // the file is staged only after the async FileSystemFileEntry callback ran
    await expect(notizenDetailPage.elements.stagedFiles().getByText('pdfFileExample.pdf'))
      .toBeVisible();

    // Regression: removing updates the staged list from an async callback that has no event
    // binding of its own - before the signal-based state, the list kept rendering the file.
    await notizenDetailPage.elements.stagedFiles().getByTestId('button-remove-staged-file').click();

    await expect(notizenDetailPage.elements.stagedFiles())
      .toHaveCount(0);

    await notizenDetailPage.elements.buttonConfirmAdd().click();

    // The Notiz saves without the attachment, and the file never appears anywhere.
    await expect(page.getByText('Die Änderungen wurden erfolgreich gespeichert')).toBeVisible();
    await expect(page.getByText('pdfFileExample.pdf'))
      .toHaveCount(0);
  });
})
