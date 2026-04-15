import {inject, Injectable} from "@angular/core";
import {patchState, signalStore} from "@ngrx/signals";
import {removeAllEntities, setAllEntities, setEntity, withEntities} from "@ngrx/signals/entities";
import {OrganisationJson, OrganisationService} from "@dvb/data-access-backend";
import {AppErrorHandlerService, Loader, withSelectedEntity} from "@dvb/util-angular";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {pipe, switchMap, tap} from "rxjs";
import {tapResponse} from "@ngrx/operators";
import {BackendEntityId} from "@dvb/data-access-types";

@Injectable({providedIn: 'root'})
export class OrganisationStore extends signalStore(
  {protectedState: false},
  withEntities<OrganisationJson>(),
  withSelectedEntity<OrganisationJson>(),
) {
  readonly #organisationService = inject(OrganisationService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  // TODO ESC-1180: nur noch loadOrganisationen, dieses gibt alle berechtigten zurück

  loadOrganisationen = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() =>
        this.loader.tapLoading$(this.#organisationService.findAllOrganisationen$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.organisationList)),
        error: console.error,
      }),
    ),
  );

  loadOrganisation = rxMethod<BackendEntityId>(
    pipe(
      switchMap(organisationId =>
        this.loader.tapLoading$(
          this.#organisationService.findOrganisation$({organisationId}),
          error => this.#appErrorHandlerService.handleError(error))
      ),
      tapResponse({
        next: result => {
          this.setSelectedEntityId(result.id);
          patchState(this, setEntity(result));
        },
        error: console.error,
      })
    )
  )
}
