import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {
  DocumentQueueDisplayJson,
  DocumentQueueIDListJson,
  DocumentQueueListSaveJson,
  DocumentQueueSaveJson,
  DocumentQueueService,
  SuperAdminRunnerService,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  AppErrorHandlerService,
  AppNotificationStore,
  handleBlobResponseDownload,
  Loader,
  rxMethod,
} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {exhaustMap, pipe} from 'rxjs';

@Injectable()
export class DocumentQueueStore extends signalStore(
  { protectedState: false },
  withDevtools('documentQueue') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<DocumentQueueDisplayJson>(),
) {
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);
  readonly #documentQueueService = inject(DocumentQueueService);
  readonly #superAdminRunnerService = inject(SuperAdminRunnerService);
  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadDocumentQueueListForCurrentBenutzer = rxMethod<void>(
    pipe(
      exhaustMap(() =>
        this.loader.tapLoading$(this.#documentQueueService.findAllByCurrentBenutzer$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => {
          patchState(this, setAllEntities(listJson.list));
        },
        error: console.error,
      }),
    ),
  );

  deleteGeneratedDocsManually = rxMethod<DocumentQueueIDListJson>(
    pipe(
      exhaustMap((list) =>
        this.loader.tapLoading$(this.#documentQueueService.removeDocumentQueueItems$({documentQueueIDListJson: list}), error => {
            this.#appErrorHandlerService.handleError(error);
          },
        ),
      ),
      tapResponse({
        next: listJson => {
          patchState(this, setAllEntities(listJson.list));
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('reports.action.manual.deletion.success'));
        },
        error: console.error,
      }),
    ),
  );

  loadDocumentQueueList = rxMethod<void>(
    pipe(
      exhaustMap(() =>
        this.loader.tapLoading$(this.#documentQueueService.findAll$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => {
          patchState(this, setAllEntities(listJson.list));
        },
        error: console.error,
      }),
    ),
  );

  download = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap((id) =>
        this.#documentQueueService.downloadFile$({ documentQueueId: id }, 'response')),
      tapResponse({
        next: result => {
          handleBlobResponseDownload(result, true);
        },
        error: console.error,
      }),
    ),
  );

  createDocumentQueue = rxMethod<DocumentQueueSaveJson>(
    pipe(
      exhaustMap((documentQueueSaveJson) => this.#documentQueueService.create$({ documentQueueSaveJson })),
    ),
  );

  forceTriggerBatchJob = rxMethod<void>(
    pipe(
      exhaustMap(() => this.#superAdminRunnerService.triggerDocumentQueueJob$()),
    ),
  );

  createMultipleDocumentQueue = rxMethod<{
    documentQueueListSaveJson: DocumentQueueListSaveJson,
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(({ documentQueueListSaveJson, onSuccess }) => this.saveLoader.tapLoading$(
        this.#documentQueueService.createMultiple$({ documentQueueListSaveJson }),
        error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: () => {
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );
}
