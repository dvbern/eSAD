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


import {extractFilenameFromContentDispositionHeader} from './blob-util';

describe('extractFilenameFromContentDispositionHeader', () => {
  it('handles standard filename with quotes', () => {
    const header = 'attachment; filename="example.pdf"';
    expect(extractFilenameFromContentDispositionHeader(header)).toBe('example.pdf');
  });

  it('handles UTF-8 encoded filename', () => {
    const header = 'attachment; filename*=UTF-8\'\'L%C3%A4nder.pdf';
    expect(extractFilenameFromContentDispositionHeader(header)).toEqual('Länder.pdf');
  });

  it('handles mixed filename with both standard and UTF-8 encodings', () => {
    const header = 'attachment; filename="19_Liste_Laender.pdf"; filename*=UTF-8\'\'19_Liste_L%C3%A4ndererfassung.pdf';
    expect(extractFilenameFromContentDispositionHeader(header)).toEqual('19_Liste_Ländererfassung.pdf');
  });

  it('returns undefined for headers without filename', () => {
    const header = 'attachment';
    expect(extractFilenameFromContentDispositionHeader(header)).toBeUndefined();
  });

  it('handles filenames with special characters', () => {
    const header = 'attachment; filename="report@2023.xlsx"';
    expect(extractFilenameFromContentDispositionHeader(header)).toBe('report@2023.xlsx');
  });
  it('handles undefined as input', () => {
    expect(extractFilenameFromContentDispositionHeader(undefined)).toBeUndefined();
  });
});
