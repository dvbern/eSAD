import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {PlzService, S3ResourceService, SuperAdminRunnerService} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, handleBlobResponseDownload, Loader} from '@dvb/util-angular';
import {isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, signalStore, SignalStoreFeature} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe} from 'rxjs';

@Injectable()
export class SystemAdminStore extends signalStore(
  { protectedState: false },
  withDevtools('systemadmin') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
) {
  readonly loader = new Loader();
  readonly #plzService = inject(PlzService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);
  readonly #s3ResourceService = inject(S3ResourceService);
  readonly #superAdminRunnerService = inject(SuperAdminRunnerService);

  importPlzFromCsv = rxMethod<void>(
    pipe(
      exhaustMap(() => this.loader
        .tapLoading$(
          this.#plzService.importPlzFromCsv$(), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: () => {
              this.#appNotificationStore.addSuccessNotification(
                this.#transloco.translate('admin.import.plz.status.success'),
              );
            },
            error: console.error,
          }),
        ),
      )),
  );

  downloadFile = rxMethod<{
    fileIdInBucket: string,
    downloadDirectly?: boolean,
    onSuccess?: (fileblob: Blob) => void
  }>(
    pipe(
      exhaustMap(
        ({ fileIdInBucket, downloadDirectly, onSuccess }) => this.loader.tapLoading$(
          this.#s3ResourceService.downloadFile$({ fileIdInbucket: fileIdInBucket }, 'response'),
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

  triggerBenutzerInaktivierenJob = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.triggerBenutzerInaktivierenJob$()
        .pipe(
          tapResponse({
            next: () => this.#appNotificationStore.addSuccessNotification("Batchjob ausgeführt"),
            error: console.error
          })
        )
      ),
    ),
  );

  triggerDocumentQueueJob = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.triggerDocumentQueueJob$()
        .pipe(
          tapResponse({
            next: () => this.#appNotificationStore.addSuccessNotification("Batchjob ausgeführt"),
            error: console.error
          })
        )
      ),
    ),
  );

  triggerDocumentQueueCleanUpJob = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.triggerDocumentQueueCleanUpJob$()
        .pipe(
          tapResponse({
            next: () => this.#appNotificationStore.addSuccessNotification("Batchjob ausgeführt"),
            error: console.error
          })
        )
      ),
    ),
  );

  triggerKinderInaktivierenJob = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.triggerKinderInaktivierenJob$()
        .pipe(
          tapResponse({
            next: () => this.#appNotificationStore.addSuccessNotification("Batchjob ausgeführt"),
            error: console.error
          })
        )
      ),
    ),
  );

  triggerReloadDuplikate = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.triggerReloadDuplikate$()
        .pipe(
          tapResponse({
            next: () => this.#appNotificationStore.addSuccessNotification("Batchjob ausgeführt"),
            error: console.error
          })
        )
      ),
    ),
  );

  normalizeKindNamen = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.normalizeKindNamen$()
        .pipe(
          tapResponse({
            next: () => this.#appNotificationStore.addSuccessNotification("Kind-Namen normalisiert"),
            error: console.error
          })
        )
      ),
    ),
  );
}
