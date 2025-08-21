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
import {expect, Page} from '@playwright/test';
import {matchUrl} from '../util/url-e2e.utils';
import {DetailPageMode} from './types';

export abstract class DetailFormWithModePageObject {
  protected abstract paths: Record<DetailPageMode, string>;
  protected abstract selector: string;

  constructor(public page: Page) {
  }

  async isCurrentPage(mode: DetailPageMode) {
    await this.page.waitForURL((url) => matchUrl(url, this.paths[mode]));
    await expect(this.page.locator(this.selector)).toBeVisible();
  }

  baseForm = {
    submitButton: () => this.page.locator('button[type=submit]'),
  }
}
