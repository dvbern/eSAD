import { expect } from '@playwright/test';
import { test } from '../../fixtures/test.fixture';
import { KinderListPageObject } from '../kinder/page-objects/kinder-list.page-object';
import { KinderPageObject } from '../kinder/page-objects/kinder.page-object';
import { NotizenPageObject } from './page-objects/notizen.page-object';
import {fileupload} from "../../util/file-upload.util";

test.describe('Notizen', () => {
  test.use({ storageState: './playwright/.auth/ESC_SUPPORT' });

  test('notiz erstellen', async ({ page, testData }) => {
    const kinderList = new KinderListPageObject(page);
    const kinderPage = new KinderPageObject(page);
    const notizFormPage = new NotizenPageObject(page)

    await kinderList.goto();
    await kinderList.elements.linkKinder().first().click();

    await kinderPage.elements.buttonAddNotiz().click();

    await notizFormPage.isCurrentPage('create');
    await notizFormPage.elements.notizTitleInput().getInput(page).fill(testData.notizTitle);
    await notizFormPage.elements.notizLabelSelect().setOption(page, 'Dispensationsgesuch');

    await fileupload(page, 'apps/esc-app-e2e/src/e2e/anlass/pdfFileExample.pdf')

    await expect(page.getByRole('link').filter({ hasText: testData.notizTitle })).toBeVisible();})
})

