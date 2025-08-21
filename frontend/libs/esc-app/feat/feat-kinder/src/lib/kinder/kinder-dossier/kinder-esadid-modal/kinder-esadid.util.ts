/*
 *
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

export function formatEntryAsEsadId(enteredValue: string): string {

  // remove all whitespaces
  const digitStr = enteredValue.replace(/\s/g, '');
  let result = '';
  // now check if it is 9 digits and if so format it in the patter xx xx xxx xx
  if (digitStr.length === 9 && !isNaN(Number(digitStr))) {
    for (let i = 0; i < digitStr.length; i++) {
      if (i === 2 || i === 4 || i === 7) {
        result += ' ';
      }
       result += digitStr.charAt(i);
    }
  } else {
    result =  enteredValue;
  }
  return result;

}
