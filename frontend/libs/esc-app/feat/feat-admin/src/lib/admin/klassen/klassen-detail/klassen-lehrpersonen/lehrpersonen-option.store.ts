import {inject, Injectable} from '@angular/core';
import {LehrpersonOptionJson, LehrpersonService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap} from 'rxjs';

@Injectable()
export class LehrpersonenOptionStore extends signalStore(
  { protectedState: false },
  // withDevtools('Lehrpersonen'),
  withEntities<LehrpersonOptionJson>(),
  withSelectedEntity<LehrpersonOptionJson>(),
) {
  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly #lehrpersonenService = inject(LehrpersonService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadLehrpersonenOptions = rxMethod<{ klasseID: BackendEntityId }>(
    pipe(
      switchMap((params) =>
        this.loader.tapLoading$(
          this.#lehrpersonenService.findAllOptionsActive$(
            {
              klasseID: params.klasseID,
              excludeKlasse: true,
            },
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setAllEntities(result.lehrpersonList)),
        error: console.error,
      }),
    ),
  );
}
