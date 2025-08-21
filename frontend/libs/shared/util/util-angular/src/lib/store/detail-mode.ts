
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

export type DetailMode = 'view' | 'edit';

export const DetailMode = {
  VIEW: 'view' as DetailMode,
  EDIT: 'edit' as DetailMode,
} as const;

/**
 * function that checks if the given string is a valid DetailMode and narrows the type
 */
export function isDetailMode(value: string): value is DetailMode {
  return value === DetailMode.VIEW || value === DetailMode.EDIT;
}


/**
 * Converts a string to DetailMode, throws error if invalid
 * @param value The string value to convert
 * @throws Error if the value is not a valid DetailMode
 * @returns DetailMode
 */
export function toDetailModeStrict(value: string): DetailMode {
  if (!isDetailMode(value)) {
    throw new Error(`Invalid DetailMode: ${value}`);
  }

  return value;
}
