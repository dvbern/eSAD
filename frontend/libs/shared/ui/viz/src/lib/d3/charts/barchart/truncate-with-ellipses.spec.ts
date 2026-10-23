
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

import {truncateWithEllipses} from './truncate-with-ellipses';
import {describe, expect} from 'vitest';

describe('barchart', () => {

  it.each([

    ['', 5, ''],
    ['a', 5, 'a'],
    ['ab', 5, 'ab'],
    ['abc', 5, 'abc'],
    ['abcd', 5, 'abcd'],
    ['abcde', 5, 'abcde'],
    ['abcdef', 5, 'abcd…'],
    ['abcdefg', 5, 'abcd…'],

    ['Gonorrhoe', -1, ''],
    ['Gonorrhoe', 0, ''],
    ['Gonorrhoe', 1, '…'],
    ['Gonorrhoe', 2, 'G…'],
    ['Gonorrhoe', 3, 'Go…'],
    ['Gonorrhoe', 4, 'Gon…'],
    ['Gonorrhoe', 8, 'Gonorrh…'],
    ['Gonorrhoe', 9, 'Gonorrhoe'],
    ['Gonorrhoe', 10, 'Gonorrhoe'],
    ['Gonorrhoe', 11, 'Gonorrhoe'],

  ])(
    'Ellipsis for \'%s\' and length \'%s\' should give \'%s\'',
    (original: string, length: number, output: string) => {

      expect(truncateWithEllipses(original, length)).toEqual(output);
    });

});
