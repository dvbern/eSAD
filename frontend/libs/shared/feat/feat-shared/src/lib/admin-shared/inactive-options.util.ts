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

import {EntityAktivStatus} from '@dvb/data-access-backend';

export function getOptionWithSuffix<T extends {
  aktivStatus: EntityAktivStatus,
  bezeichnung: string
}>(translatedSuffix: string, currentOption: T) : T;
export function getOptionWithSuffix<T extends {
  aktivStatus: EntityAktivStatus,
  bezeichnung: string
}>(translatedSuffix: string, currentOption: T | undefined) : T | undefined;

export function getOptionWithSuffix<T extends {
  aktivStatus: EntityAktivStatus,
  bezeichnung: string
}>(translatedSuffix: string, currentOption: T | undefined) : T | undefined {
  if(currentOption?.aktivStatus === EntityAktivStatus.INAKTIV){
    return {
      ...currentOption,
      bezeichnung: currentOption.bezeichnung + translatedSuffix,
    };
  }
  return currentOption
}