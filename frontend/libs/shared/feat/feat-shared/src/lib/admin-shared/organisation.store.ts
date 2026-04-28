import {inject, Injectable} from "@angular/core";
import {patchState, signalStore} from "@ngrx/signals";
import {removeAllEntities, setAllEntities, withEntities} from "@ngrx/signals/entities";
import {OrganisationJson, OrganisationService} from "@dvb/data-access-backend";
import {AppErrorHandlerService, Loader, withSelectedEntity} from "@dvb/util-angular";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {pipe, switchMap, tap} from "rxjs";
import {tapResponse} from "@ngrx/operators";

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

  loadOrganisationen = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() =>
        this.loader.tapLoading$(this.#organisationService.findOrganisationenBerechtigt$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.organisationList)),
        error: console.error,
      }),
    ),
  );
}
