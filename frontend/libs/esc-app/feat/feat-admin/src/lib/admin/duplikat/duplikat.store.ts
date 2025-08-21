import {inject, Injectable} from '@angular/core';
import {DuplikateService, DuplikatJson} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader, withSelectedEntity} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';
import {DuplikatMergeJson} from './duplikat-merge/duplikat-merge.form';

@Injectable()
export class DuplikatStore extends signalStore(
  {protectedState: false},
  // withDevtools('label'),
  withEntities<DuplikatJson>(),
  withSelectedEntity<DuplikatJson>(),
) {
  readonly #duplikatStore = inject(DuplikateService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly loaderSave = new Loader();

  loadDuplikate = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(this.#duplikatStore.findAllDuplikate$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: duplikatList => patchState(this, setAllEntities(duplikatList.duplikatList)),
        error: console.error,
      }),
    ),
  );

  reloadDuplikate = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(this.#duplikatStore.reloadDuplikate$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: duplikatList => {
          patchState(this, setAllEntities(duplikatList.duplikatList));
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('duplikat.list.reload.success'));
        },
        error: console.error,
      }),
    ),
  );

  markDuplikatAsFalsePositive = rxMethod<DuplikatJson>(
    pipe(
      exhaustMap((duplikat) => this.loaderSave.tapLoading$(
        this.#duplikatStore.markAsFalsePositive$({
          duplikatId: duplikat.id,
        }),
        error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
            },
            error: console.error,
          }),
        )),
    ),
  );

  merge = rxMethod<DuplikatMergeJson>(
    pipe(
      exhaustMap((payload) => this.loaderSave.tapLoading$(
        this.#duplikatStore.merge$({
          duplikatId: payload.duplikatId,
          basisKindId: payload.basisKindId,
        }),
        error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
              this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('duplikat.merge.success'));
            },
            error: console.error,
          }),
        )),
    ),
  );

}
