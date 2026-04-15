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

import {Locator, Page} from '@playwright/test';

type Host = 'svui-form-control' | 'svui-table-control' | 'ng-select' ;

export class FormSelectComponentObject {
  constructor(
    private testId: string,
    private host: Host,
  ) {}

  getFormField = (page: Page | Locator) => page.locator(`${this.host}[data-testid=${this.testId}]`);

  getSelect = (page: Page | Locator) => this.getFormField(page).getByRole('combobox');

  async setOption(page: Page, option: string, row?: Locator) {
    await this.getSelect(row ?? page).click();
    await page.getByRole('option', { name: option }).click();
    if(this.host !== 'svui-table-control') {
      await this.getSelect(page).blur();
    }
  }
}
