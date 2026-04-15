import {Page} from "@playwright/test";
import {NotizenDetailPageObject} from "../e2e/notizen/page-objects/notizen-detail.page-object";
import path from "path";

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

  await page.waitForTimeout(500);

  await notizenDetailPage.elements.buttonConfirmAdd().click()
}
