/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {MassenimportService} from '@dvb/data-access-backend';
import {AppErrorHandlerService, handleBlobResponseDownload, Loader} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {signalStore} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe} from 'rxjs';
import {KinderImportJson} from './kinder-import.form';

export interface KinderImportJsonEnhanced extends KinderImportJson {
  file?: File
}

@Injectable({providedIn: 'root'})
export class KinderImportStore extends signalStore(
  {protectedState: false},
  withDevtools('kinderImport'),
) {

  readonly #massenimportService = inject(MassenimportService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly importLoader = new Loader();

  downloadStandardTemplate = rxMethod<void>(
    pipe(
      exhaustMap(() => this.importLoader.tapLoading$(
          this.#massenimportService.downloadStandardTemplate$('response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(tapResponse({
            next: result => {
              handleBlobResponseDownload(result, true);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  downloadKlassenTemplate = rxMethod<void>(
    pipe(
      exhaustMap(() => this.importLoader.tapLoading$(
          this.#massenimportService.downloadKlassenTemplate$('response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              handleBlobResponseDownload(result, true);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  importKinderStandard = rxMethod<{
    kinderImportJson: KinderImportJsonEnhanced,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        ({kinderImportJson, onSuccess}) => this.importLoader.tapLoading$(
          this.#massenimportService.massenimportKinderForSchuleAndKlasse$({
            klasseID: requireValue(kinderImportJson.klasse?.id),
            schuleID: requireValue(kinderImportJson.schule?.id),
            file: requireValue(kinderImportJson.file),
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: () => onSuccess(),
            error: console.error,
          }),
        ),
      ),
    ),
  );

  importKinderKlassen = rxMethod<{
    kinderImportJson: KinderImportJsonEnhanced,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        ({kinderImportJson, onSuccess}) => this.importLoader.tapLoading$(
          this.#massenimportService.massenimportKinderForSchuleExtractKlassen$({
            schuleID: requireValue(kinderImportJson.schule?.id),
            file: requireValue(kinderImportJson.file),
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: () => onSuccess(),
            error: console.error,
          }),
        ),
      ),
    ),
  );
}

export function enhanceKinderImportJson(kinderImport: KinderImportJson, fileUpload: File): KinderImportJsonEnhanced {
  return {
    ...kinderImport,
    file: fileUpload,
  };
}
