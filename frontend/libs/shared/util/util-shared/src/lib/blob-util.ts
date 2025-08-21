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

/**
 * apparently when setting.anchor.download the browser will download directly, but if the filename is not set
 * the browser will open the file in a new tab
 */
export function openBlobInNewTab(blob: Blob): void {
  const downloadUrl = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = downloadUrl;
  anchor.target = '_blank'; // open in new tab
  anchor.click();
}

export function downloadBlobDirectly(blob: Blob, filename?: string): void {
  if(!filename){
    console.warn('filename is not set, the browser will open the file in a new tab');
  }
  const downloadUrl = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = downloadUrl;
  if(filename) {
    anchor.download = filename;
  }

  anchor.target = '_blank'; // open in new tab in case anything goes wrong
  anchor.click();
}

/**
 * match filenames lik
 * attachment; filename="19_Liste_La?ndererfassung.pdf"; filename*=UTF-8''19_Liste_La%CC%88ndererfassung.pdf
 * @param contentDisposition
 */
export function extractFilenameFromContentDispositionHeader(contentDisposition: string | null | undefined): string | undefined{
  if (!contentDisposition) {
    return undefined;
  }
  // First, look for UTF-8 encoded filename
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;\s]+)/i);
  if (utf8Match) {
    try {
      return decodeURIComponent(utf8Match[1]);
    } catch (error) {
      console.warn('Failed to decode UTF-8 filename', error);
    }
  }

  // Fallback to standard filename
  const standardMatch = contentDisposition.match(/filename="?([^";\s]+)"?/i);
  if (standardMatch) {
    try {
      return decodeURIComponent(standardMatch[1].replace(/['"]/g, ''));
    } catch (error) {
      console.warn('Failed to decode standard filename', error);
    }
  }

  return undefined;
}

