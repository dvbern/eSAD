import {expect, Page} from '@playwright/test';
import {matchUrl} from "../util/url-e2e.utils";


export abstract class WithSubpaths<T extends string> {
  protected abstract paths: Record<T, string>;
  protected abstract selector: string;

  constructor(public page: Page) {
  }

  async isCurrentPage(pathKey: T) {
    const path = this.paths[pathKey];
    await this.page.waitForURL((url) => matchUrl(url, path), { waitUntil: 'domcontentloaded', timeout: 20000});
    await expect(this.page.locator(this.selector).first()).toBeVisible({ timeout: 20000 });
  }

  baseForm = {
    submitButton: () => this.page.locator('button[type=submit]'),
  }
}

