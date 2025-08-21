import {inject, Injectable} from '@angular/core';
import {KontaktPersonKindOptionJson, KontaktpersonKindService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap} from 'rxjs';

@Injectable()
export class KontaktPersonKindOptionsStore extends signalStore(
  { protectedState: false },
  // withDevtools('kontaktPersonKind'),
  withEntities<KontaktPersonKindOptionJson>(),
  withSelectedEntity<KontaktPersonKindOptionJson>(),
) {
  readonly #kontaktPersonKindService = inject(KontaktpersonKindService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  loadKontaktPersonenByKindOptions = rxMethod<{ kindID: BackendEntityId }>(
    pipe(
      switchMap((params) =>
        this.loader.tapLoading$(
          this.#kontaktPersonKindService.findAllOptionsActive$(
            {
              kindID: params.kindID,
              excludeKind: true,
            },
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setAllEntities(result.kontaktPersonList)),
        error: console.error,
      }),
    ),
  );
}
