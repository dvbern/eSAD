import {Page} from "@playwright/test";

export class TableComponentObject {

  constructor(private testId: string) {
  }

  row = (page: Page, row: number) => page.locator(`table > tbody > tr>>nth=${row}`);
}
