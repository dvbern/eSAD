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

import {HttpResponse} from '@angular/common/http';
import {
  downloadBlobDirectly,
  extractFilenameFromContentDispositionHeader,
  openBlobInNewTab,
  requireValue,
} from '@dvb/util-shared';

export function handleBlobResponseDownload(httpResponse: HttpResponse<Blob>, directDownload: boolean): void {
  const contentDisposition = httpResponse.headers.get('Content-Disposition');
  const filename = extractFilenameFromContentDispositionHeader(contentDisposition);
  const blob = requireValue(httpResponse.body);
  if (directDownload || filename?.endsWith('.msg')) {
    downloadBlobDirectly(blob, filename);
  } else {
    openBlobInNewTab(blob);
  }
}
