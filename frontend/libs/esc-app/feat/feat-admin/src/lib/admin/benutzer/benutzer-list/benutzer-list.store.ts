import {inject, Injectable} from '@angular/core';
import {BenutzerJson, BenutzerSaveJson, BenutzerService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {removeAllEntities, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';

@Injectable()
export class BenutzerListStore extends signalStore(
  {protectedState: false},
  // withDevtools('benutzerList'),
  withEntities<BenutzerJson>(),
  withSelectedEntity<BenutzerJson>(),
) {
  readonly #benutzerService = inject(BenutzerService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly loaderSave = new Loader();

  loadBenutzerList = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() => this.loader.tapLoading$(
        this.#benutzerService.findAllBenutzer$(),
        error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse({
        next: benutzerList => patchState(this, setAllEntities(benutzerList.benutzerList)),
        error: console.error,
      }),
    ),
  );

  toggleActiveOnBenutzer = rxMethod<BenutzerJson>(
    pipe(
      exhaustMap((benutzerJson) => this.loaderSave
        .tapLoading$(
          this.#benutzerService.updateBenutzerAktivStatus$({
            benutzerID: benutzerJson.id,
            benutzerSaveJson: {...benutzerJson.data, aktiv: !benutzerJson.data.aktiv},
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
            },
            error: console.error,
          }),
        ),
      )),
  );

  updateBenutzer = rxMethod<{ benutzerID: BackendEntityId, saveJson: BenutzerSaveJson }>(
    pipe(
      exhaustMap(({benutzerID, saveJson}) => this.loaderSave
        .tapLoading$(
          this.#benutzerService.update$({
            benutzerID: benutzerID,
            benutzerSaveJson: saveJson,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
            },
            error: console.error,
          }),
        ),
      )),
  );
}
