import {inject, Injectable} from '@angular/core';
import {KontaktPersonSchuleOptionJson, KontaktpersonSchuleService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap} from 'rxjs';

@Injectable()
export class KontaktPersonSchuleOptionsStore extends signalStore(
  { protectedState: false },
  // withDevtools('kontaktPersonSchule'),
  withEntities<KontaktPersonSchuleOptionJson>(),
  withSelectedEntity<KontaktPersonSchuleOptionJson>(),
) {
  readonly #kontaktPersonSchuleService = inject(KontaktpersonSchuleService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  loadKontaktPersonenExcludeSchule = rxMethod<{ schuleID: BackendEntityId }>(
    pipe(
      switchMap((params) =>
        this.loader.tapLoading$(
          this.#kontaktPersonSchuleService.findAllOptionsActive$(
            {
              schuleID: params.schuleID,
              excludeSchule: true,
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
