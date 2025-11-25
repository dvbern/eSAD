import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {NotizenService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, handleBlobResponseDownload, Loader} from '@dvb/util-angular';
import {isNotNullish, requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, signalStore, SignalStoreFeature, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe} from 'rxjs';

@Injectable({providedIn: 'root'})
export class NotizenStore extends signalStore(
  {protectedState: false},
  withDevtools('notizen') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState({ dummyProp: 0 }), // can not decalre empty state
) {
  readonly #notizenService = inject(NotizenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();

  downloadNotizFile = rxMethod<{
    notizId: BackendEntityId,
    fileId: BackendEntityId,
    downloadDirectly?: boolean,
    onSuccess?: (fileblob: Blob) => void
  }>(
    pipe(
      exhaustMap(
        ({notizId, fileId, downloadDirectly, onSuccess}) => this.loader.tapLoading$(
          this.#notizenService.downloadNotizFileSync$(
            {
              notizId: notizId,
              fileId: fileId,
            }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              handleBlobResponseDownload(result, downloadDirectly ?? false);
              if (isNotNullish(onSuccess)) {
                onSuccess(requireValue(result.body));
              }
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );
}
