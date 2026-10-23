import {expect, Page} from "@playwright/test";
import {NotizenDetailPageObject} from "../e2e/notizen/page-objects/notizen-detail.page-object";
import path from "path";

/**
 * Uploads a file on a Notiz detail page and confirms the upload dialog. Leaves the browser on the
 * Notiz detail page - the caller decides where to go from there.
 */
export async function fileupload(page: Page, filePath: string) {

  const notizenDetailPage = new NotizenDetailPageObject(page);

  const file = path.join(
    process.cwd(),
    filePath,
  );

  const [fileChooser] = await Promise.all([
    page.waitForEvent('filechooser'),
    notizenDetailPage.elements.buttonAddFile().click(),
  ]);

  await fileChooser.setFiles(file);

  // Wait for the file to be staged instead of sleeping a fixed 500ms: confirming before the file
  // is read saves a Notiz without any attachment, and the test only notices much later.
  await expect(notizenDetailPage.elements.stagedFiles().getByText(path.basename(file))).toBeVisible();

  await notizenDetailPage.elements.buttonConfirmAdd().click();

  // Confirming saves the Notiz - creating it first when it did not exist yet - and only then lists
  // the file. Wait for the save to be confirmed, not just for the row: navigating away while the
  // save is still in flight aborts it, and the file silently never arrives.
  await expect(notizenDetailPage.elements.uploadedFilesTable().locator('tbody tr').first()).toBeVisible();
  await expect(page.getByText('Die Änderungen wurden erfolgreich gespeichert')).toBeVisible();
}
