import {computed, inject, Injectable} from '@angular/core';
import {AppErrorHandlerService, handleBlobResponseDownload, Loader} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';
import {
  ArchivdokumentJson,
  ArchivdokumentService,
} from "@dvb/data-access-backend";
import {BackendEntityId} from "@dvb/data-access-types";

type ArchivModus = 'OFFEN' | 'ABGESCHLOSSEN';

type ArchivDokumentStore = {
  offeneArchivDokumente: ArchivdokumentJson[];
  abgeschlosseneArchivDokumente: ArchivdokumentJson[];
  modus: ArchivModus;
};

const initialState: ArchivDokumentStore = {
  offeneArchivDokumente: [],
  abgeschlosseneArchivDokumente: [],
  modus: 'OFFEN',
};

@Injectable()
export class ArchivierungStore extends signalStore(
  {protectedState: false},
  // withDevtools('archivierung'),
  withState(initialState),
  withComputed(({modus}) => ({
      isArchivModusOffen: computed(() => modus() === 'OFFEN')
    })
  ),
) {
  readonly #archivService = inject(ArchivdokumentService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();

  setArchivModus(modus: ArchivModus) {
    patchState(this, {modus});
  }

  loadArchivDokumente = rxMethod<boolean>(
    pipe(
      tap(() => patchState(this, {
        offeneArchivDokumente: [],
        abgeschlosseneArchivDokumente: [],
      })),
      switchMap((onlyOpen: boolean) =>
        this.loader.tapLoading$(this.#archivService.findAllArchivdokumente$({onlyOpen}), error =>
          this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              if (onlyOpen) {
                patchState(this, {offeneArchivDokumente: result.archivdokumentList});
              } else {
                patchState(this, {abgeschlosseneArchivDokumente: result.archivdokumentList});
              }
            },
            error: console.error,
          })
        ),
      ),
    ),
  );

  downloadArchivdokument = rxMethod<{
    archivdokumentId: BackendEntityId,
    reloadOffeneArchivDokumente: boolean,
  }>(
    pipe(
      exhaustMap(
        ({archivdokumentId, reloadOffeneArchivDokumente}) => this.saveLoader.tapLoading$(
          this.#archivService.downloadArchivFile$({
            archivdokumentId
          }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: (result) => {
              handleBlobResponseDownload(result, true);
              this.loadArchivDokumente(reloadOffeneArchivDokumente);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );
}
