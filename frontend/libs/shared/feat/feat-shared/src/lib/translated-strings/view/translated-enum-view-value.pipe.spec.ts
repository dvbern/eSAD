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

import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { TranslatedEnumViewValuePipe } from './translated-enum-view-value.pipe';
import type { Mock } from 'vitest';

describe('TranslatedEnumViewValuePipe', () => {
  let translateMock: Mock<(key: string) => string>;

  beforeEach(() => {
    translateMock = vi.fn((key: string) => key);

    TestBed.configureTestingModule({
      providers: [
        { provide: TranslocoService, useValue: { translate: translateMock } },
      ],
    });
  });

  function createPipe(): TranslatedEnumViewValuePipe {
    return TestBed.runInInjectionContext(() => new TranslatedEnumViewValuePipe());
  }

  it('should return empty string for nullish values', () => {
    const pipe = createPipe();

    expect(pipe.transform(null, 'Prefix')).toBe('');
    expect(pipe.transform(undefined, 'Prefix')).toBe('');
    expect(translateMock).not.toHaveBeenCalled();
  });

  it('should translate a single string value with prefix', () => {
    const pipe = createPipe();

    const result = pipe.transform('VALUE', 'My.Prefix');

    expect(result).toBe('My.Prefix.VALUE');
    expect(translateMock).toHaveBeenCalledTimes(1);
    expect(translateMock).toHaveBeenCalledWith('My.Prefix.VALUE');
  });

  it('should translate an array of values with default separator', () => {
    const pipe = createPipe();

    const result = pipe.transform(['A', 'B', 'C'], 'Enum.Prefix');

    expect(result).toBe('Enum.Prefix.A, Enum.Prefix.B, Enum.Prefix.C');
    expect(translateMock).toHaveBeenCalledTimes(3);
    expect(translateMock).toHaveBeenNthCalledWith(1, 'Enum.Prefix.A');
    expect(translateMock).toHaveBeenNthCalledWith(2, 'Enum.Prefix.B');
    expect(translateMock).toHaveBeenNthCalledWith(3, 'Enum.Prefix.C');
  });

  it('should translate an array of values with a custom separator', () => {
    const pipe = createPipe();

    const result = pipe.transform(['X', 'Y'], 'Prefix', ' | ');

    expect(result).toBe('Prefix.X | Prefix.Y');
    expect(translateMock).toHaveBeenCalledTimes(2);
    expect(translateMock).toHaveBeenNthCalledWith(1, 'Prefix.X');
    expect(translateMock).toHaveBeenNthCalledWith(2, 'Prefix.Y');
  });

  it('should handle empty prefix as implemented (leading dot)', () => {
    const pipe = createPipe();

    const single = pipe.transform('FOO');
    const multiple = pipe.transform(['FOO', 'BAR']);

    expect(single).toBe('FOO');
    expect(multiple).toBe('FOO, BAR');
    expect(translateMock).toHaveBeenCalledWith('FOO');
    expect(translateMock).toHaveBeenCalledWith('BAR');
  });
});
