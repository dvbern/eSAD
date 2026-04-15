import {inject, Injectable} from '@angular/core';
import {
  BenutzerJson,
  BenutzerSaveJson,
  BenutzerService,
  EntityAktivStatus,
  FachRolle
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {addEntity, removeAllEntities, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';
import {requireValue} from "@dvb/util-shared";
import {BackendEntityId} from "@dvb/data-access-types";

export interface BenutzerEnhancedJson extends BenutzerJson {
  fachRollen: FachRolle[];
}

@Injectable({providedIn: 'root'})
export class BenutzerListStore extends signalStore(
  {protectedState: false},
  // withDevtools('benutzerList'),
  withEntities<BenutzerEnhancedJson>(),
  withSelectedEntity<BenutzerEnhancedJson>(),
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
        next: benutzerList => patchState(this,
          setAllEntities(benutzerList.benutzerList.map(benutzer => this.enhanceBenutzer(benutzer)))),
        error: console.error,
      }),
    ),
  );

  loadBenutzerListForOrganisation = rxMethod<BackendEntityId>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap((organisationID) => this.loader.tapLoading$(
        this.#benutzerService.findAllBenutzerForOrganisation$({organisationID}),
        error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse({
        next: benutzerList => patchState(this,
          setAllEntities(benutzerList.benutzerList.map(benutzer => this.enhanceBenutzer(benutzer)))),
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
            benutzerSaveJson: {
              ...benutzerJson.data,
              benutzerAktivStatus: benutzerJson.data.benutzerAktivStatus == EntityAktivStatus.AKTIV ?
                EntityAktivStatus.INAKTIV :
                EntityAktivStatus.AKTIV
            },
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(this.enhanceBenutzer(result)));
            },
            error: console.error,
          }),
        ),
      )),
  );

  createBenutzer = rxMethod<{ saveJson: BenutzerSaveJson, onSuccess: () => void; }>(
    pipe(
      exhaustMap(({saveJson, onSuccess}) => this.loaderSave
        .tapLoading$(
          this.#benutzerService.createBenutzer$({
            benutzerSaveJson: saveJson,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(this.enhanceBenutzer(result)));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      )),
  );

  updateBenutzer = rxMethod<{ saveJson: BenutzerSaveJson, onSuccess: () => void; }>(
    pipe(
      exhaustMap(({saveJson, onSuccess}) => this.loaderSave
        .tapLoading$(
          this.#benutzerService.update$({
            benutzerID: requireValue(this.selectedEntity()?.id),
            benutzerSaveJson: saveJson,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(this.enhanceBenutzer(result)));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      )),
  );

  enhanceBenutzer(benutzer: BenutzerJson): BenutzerEnhancedJson {
    let combinedFachrollen: FachRolle[] = [];
    if (benutzer.data.systemRollen != null) {
      combinedFachrollen = [...benutzer.data.systemRollen.fachRollen];
    } else if (benutzer.data.organisationRollen.length > 0) {
      combinedFachrollen = [...benutzer.data.organisationRollen[0].fachRollen];
    }
    return {
      ...benutzer,
      fachRollen: combinedFachrollen,
    };
  }
}
