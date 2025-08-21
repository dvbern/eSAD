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



import {FileDropComponent} from './file-drop.component';

describe('validateFileType', () => {
  // Helper function to create File objects for testing
  const createFile = (name: string, type: string): File => {
    return new File([''], name, { type });
  };

  describe('MIME type validation', () => {
    it('should return true when file MIME type matches accepted types', () => {
      const file = createFile('document.pdf', 'application/pdf');
      const acceptedTypes = 'application/pdf,image/jpeg';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should handle multiple MIME types correctly', () => {
      const file = createFile('image.jpg', 'image/jpeg');
      const acceptedTypes = 'application/pdf,image/jpeg,image/png';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should return false when MIME type is not in accepted types', () => {
      const file = createFile('document.doc', 'application/msword');
      const acceptedTypes = 'application/pdf,image/jpeg';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(false);
    });
  });

  describe('File extension validation', () => {
    it('should return true when file extension matches accepted types', () => {
      const file = createFile('document.pdf', '');
      const acceptedTypes = '.pdf,.jpg';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should handle multiple extensions correctly', () => {
      const file = createFile('image.jpg', '');
      const acceptedTypes = '.pdf,.jpg,.png';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should return false when extension is not in accepted types', () => {
      const file = createFile('document.doc', '');
      const acceptedTypes = '.pdf,.jpg';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(false);
    });

    it('should handle uppercase file extensions', () => {
      const file = createFile('document.PDF', '');
      const acceptedTypes = '.pdf,.jpg';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });
  });

  describe('Edge cases', () => {
    it('should handle files with multiple dots in name', () => {
      const file = createFile('my.special.document.pdf', 'application/pdf');
      const acceptedTypes = 'application/pdf,.pdf';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should handle spaces in accepted types', () => {
      const file = createFile('document.pdf', 'application/pdf');
      const acceptedTypes = ' application/pdf , .pdf ';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should handle files without extensions', () => {
      const file = createFile('README', 'text/plain');
      const acceptedTypes = 'text/plain,.txt';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

    it('should handle empty accepted types string', () => {
      const file = createFile('document.pdf', 'application/pdf');
      const acceptedTypes = '';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(false);
    });

    it('should handle whitespaced accepted types string', () => {
      const file = createFile('document.pdf', 'application/pdf' );
      const acceptedTypes = '.jpg, image/gif,  .pdf , .png';

      expect(FileDropComponent.validateFileType(file, acceptedTypes)).toBe(true);
    });

  });
});
