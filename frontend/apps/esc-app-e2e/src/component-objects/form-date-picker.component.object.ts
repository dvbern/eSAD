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

import {Page} from '@playwright/test';
import {format} from 'date-fns';

export class FormDatePickerComponentObject {

  constructor(private readonly testId: string) {
  }

  getFormField = (page: Page) => page.locator(`svui-form-control[data-testid=${this.testId}]`);

  getInput = (page: Page) => this.getFormField(page).locator('input');

  async setDate(page: Page, date: Date) {
    // The app renders native <input type="date">. Typing the segments one by one would follow the
    // browser locale (de-CH expects dd.MM.yyyy, en-US MM/dd/yyyy), which silently swapped day and
    // month. fill() takes the value in the wire format instead and is locale independent.
    await this.getInput(page).fill(format(date, 'yyyy-MM-dd'));
    await this.getInput(page).blur();
  }
}
