import {expect, Page} from '@playwright/test';

/**
 * Asserts that an Aufgabe is in the Aufgaben list, filtering for it by title.
 *
 * The Aufgabe is created by the backend as a side effect of whatever the test did, and the list is
 * fetched once and then filtered in the browser - so re-typing the filter would never show an
 * Aufgabe that landed after the page was opened. The page is reloaded until it turns up.
 *
 * @param titleFilter what to type into the Titel column filter, typically the Anlass title
 * @param expectedTitle the full Aufgaben title the row has to carry
 */
export async function expectAufgabeInList(page: Page, titleFilter: string, expectedTitle: RegExp) {
  const aufgabenRow = page.getByRole('row').filter({hasText: expectedTitle});

  await expect(async () => {
    await page.reload();

    const filterInput = page.getByRole('textbox', {name: 'Filter Titel'});
    await filterInput.fill(titleFilter);

    await expect(aufgabenRow).toBeVisible({timeout: 2000});
  }).toPass({timeout: 30000});
}
