import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {
  EntityAktivStatusSaveJson,
  EscadaKlasseStufe,
  KlasseJson,
  KlassenService,
  KlasseSaveJson,
  SchulenService,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withMethods} from '@ngrx/signals';
import {
  addEntity,
  removeAllEntities,
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities,
} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';
import {getOptionWithSuffix} from './inactive-options.util';

export interface KlasseJsonEnhanced extends KlasseJson {
  lehrpersonenName: string;
}

@Injectable({providedIn: 'root'})
export class KlassenStore extends signalStore(
  {protectedState: false},
  withDevtools('klassen') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<KlasseJsonEnhanced>(),
  withSelectedEntity<KlasseJsonEnhanced>(),
  withMethods((store, translocoService = inject(TranslocoService)) => ({
    getKlassenstufeTranslatedList(escadaKlassenStufe: EscadaKlasseStufe[]): string {
      return escadaKlassenStufe.map(escadaKlassenStufe => translocoService.translate(`EscadaKlasseStufe.${escadaKlassenStufe}`))
        .join(', ');
    },
  })),
) {
  readonly loader = new Loader();
  readonly loadKlassenBySchuleLoader = new Loader();
  readonly loadEntityLoader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly changeStatusLoader = new Loader();
  readonly #klassenService = inject(KlassenService);
  readonly #schulenService = inject(SchulenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #transloco = inject(TranslocoService);

  loadKlassenList = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() => this.loader.tapLoading$(this.#klassenService.findAllKlassen$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this,
          setAllEntities(listJson.klassenList.map(klasse => this.enhanceKlasse(klasse)))),
        error: console.error,
      }),
    ),
  );

  loadKlassenBySchuleList = rxMethod<{ schuleId: BackendEntityId }>(
    pipe(
      tap(() => patchState(this, setAllEntities([] as KlasseJsonEnhanced[]))),
      switchMap((params) => this.loadKlassenBySchuleLoader.tapLoading$(
        this.#schulenService.findSchuleKlassen$({schuleId: params.schuleId}),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this,
          setAllEntities(listJson.klassenList.map(klasse => this.enhanceKlasse(klasse)))),
        error: console.error,
      }),
    ),
  );

  createKlasse = rxMethod<{ klasse: KlasseSaveJson, onSuccess: (result: KlasseJson) => void }>(
    pipe(
      exhaustMap(
        ({
           klasse,
           onSuccess,
         }) => this.saveLoader.tapLoading$(this.#klassenService.createKlasse$({klasseSaveJson: klasse}),
          error => this.#appErrorHandlerService.handleError(error))
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, addEntity(this.enhanceKlasse(result)));
                onSuccess(result);
              },
              error: error => this.#appErrorHandlerService.handleError((error)),
            }),
          ),
      ),
    ),
  );

  loadEntity = rxMethod<BackendEntityId>(
    pipe(
      switchMap((entityId) => this.loadEntityLoader
        .tapLoading$(this.#klassenService
            .findKlasse$({klasseId: entityId}),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setEntity(this.enhanceKlasse(result))),
        error: console.error,
      }),
    ),
  );

  updateKlasse = rxMethod<{ klasseToUpdate: KlasseSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(({klasseToUpdate, onSuccess}) =>
        this.saveLoader.tapLoading$(
          this.#klassenService.updateKlasse$({
            klasseId: requireValue(this.selectedEntity()?.id),
            klasseSaveJson: klasseToUpdate,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(this.enhanceKlasse(result)));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  changeKlasseAktivStatus = rxMethod<{ klasseId: BackendEntityId, saveJson: EntityAktivStatusSaveJson }>(
    pipe(
      exhaustMap(({klasseId, saveJson}) =>
        this.changeStatusLoader
          .tapLoading$(this.#klassenService.updateKlasseAktivStatus$({
              klasseId: klasseId,
              entityAktivStatusSaveJson: saveJson,
            }),
            error =>
              this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => patchState(this, setEntity(this.enhanceKlasse(result))),
              error: console.error,
            }),
          ),
      ),
    ),
  );

  removeKlasse = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete => this.deleteLoader.tapLoading$(
        this.#klassenService.removeKlasse$({klasseId: idToDelete}),
        error => this.#appErrorHandlerService.handleError(error),
      ).pipe(
        tapResponse({
          next: () => patchState(this, removeEntity(idToDelete)),
          error: console.error,
        }),
      )),
    ),
  );

  private enhanceKlasse(klasse: KlasseJson): KlasseJsonEnhanced {
    return {
      ...klasse,
      data : {
        ...klasse.data,
        schule: getOptionWithSuffix(this.#transloco.translate('gloabl.suffixInaktiv'), klasse.data.schule)
      },
      lehrpersonenName: klasse.lehrpersonen?.map(l => l.fullName).join(', ') ?? '',

    }
  }
}
