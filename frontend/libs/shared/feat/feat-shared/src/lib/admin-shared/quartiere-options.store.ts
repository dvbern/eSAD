import {inject, Injectable} from '@angular/core';
import {QuartiereService, QuartierOptionJson} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class QuartiereOptionsStore extends signalStore(
  {protectedState: false},
  // withDevtools('quartiere'),
  withEntities<QuartierOptionJson>(),
  withSelectedEntity<QuartierOptionJson>(),
) {
  readonly #quartiereService = inject(QuartiereService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  loadQuartiereList = rxMethod<void>(
    pipe(
      tap(() => patchState(this, setAllEntities([] as QuartierOptionJson[]))),
      switchMap(() => this.loader.tapLoading$(this.#quartiereService.findAllQuartierOptions$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.quartiereList)),
        error: console.error,
      }),
    ),
  );
}
